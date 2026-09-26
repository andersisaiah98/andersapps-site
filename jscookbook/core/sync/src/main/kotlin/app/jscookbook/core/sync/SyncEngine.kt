package app.jscookbook.core.sync

import app.jscookbook.core.data.photo.PhotoStore
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.sync.LocalSync
import app.jscookbook.core.data.sync.SyncTable
import app.jscookbook.core.data.sync.SyncTables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncResult {
    data object Success : SyncResult
    /** Not configured, not signed in, or not in a household yet: nothing to do. */
    data object Skipped : SyncResult
    data class Failed(val error: Throwable) : SyncResult
}

sealed interface SyncActivity {
    data object Idle : SyncActivity
    data object Syncing : SyncActivity
    data class Failed(val message: String) : SyncActivity
}

/**
 * One sync pass: upload new photos, push rows changed on this phone, pull rows changed on the
 * server since the last pass (last write wins), then download photos added on the other phone.
 * Room stays the only thing screens read; this just keeps it in step with the server.
 */
@Singleton
class SyncEngine @Inject constructor(
    private val remote: SyncRemote,
    private val local: LocalSync,
    private val photos: PhotoStore,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    private val _activity = MutableStateFlow<SyncActivity>(SyncActivity.Idle)
    val activity: StateFlow<SyncActivity> = _activity.asStateFlow()

    suspend fun sync(): SyncResult = mutex.withLock {
        val cookbookId = local.linkedCookbook()
        if (cookbookId == null || !remote.isReady()) return@withLock SyncResult.Skipped
        _activity.value = SyncActivity.Syncing
        try {
            val photoFailures = uploadPhotos(cookbookId)
            // Edits made while a pass runs stay dirty; go round again so they don't wait for the next trigger.
            var rounds = 0
            do {
                push(cookbookId)
                rounds++
            } while (rounds < MaxPushRounds && hasPushable())
            pull(cookbookId)
            val downloadFailures = downloadPhotos(cookbookId)
            local.setLastSyncedAt(clock.now())
            val failures = photoFailures + downloadFailures
            if (failures.isNotEmpty()) throw failures.first()
            _activity.value = SyncActivity.Idle
            SyncResult.Success
        } catch (e: CancellationException) {
            _activity.value = SyncActivity.Idle
            throw e
        } catch (e: Exception) {
            _activity.value = SyncActivity.Failed(e.message ?: e::class.simpleName.orEmpty())
            SyncResult.Failed(e)
        }
    }

    private suspend fun hasPushable(): Boolean =
        local.observePendingCount().first() > 0 && SyncTables.all.any { local.pendingPush(it).isNotEmpty() }

    private suspend fun push(cookbookId: String) {
        for (table in SyncTables.all) {
            // Only rows of the linked cook book; anything else would be refused by RLS anyway.
            val rows = local.pendingPush(table).filter { it.json.cookbookOf(table) == cookbookId }
            rows.chunked(PushBatch).forEach { batch ->
                remote.upsert(table.name, table.conflictColumns, batch.map { it.json })
                local.markPushed(table, batch)
            }
        }
    }

    private suspend fun pull(cookbookId: String) {
        for (table in SyncTables.all) {
            val cursor = local.cursor(table, cookbookId)
            // Re-read a short window before the cursor: a write that committed late can carry an
            // earlier server timestamp. Re-applying a row is harmless.
            val since = (cursor - PullOverlapMs).coerceAtLeast(0)
            var offset = 0L
            var newest = cursor
            while (true) {
                val rows = remote.fetchSince(table.name, cookbookId, since, offset, PullPage)
                local.applyRemote(table, rows)
                rows.forEach { row ->
                    val stamp = (row["server_updated_at"] as? JsonPrimitive)?.longOrNull ?: 0L
                    if (stamp > newest) newest = stamp
                }
                if (rows.size < PullPage) break
                offset += rows.size
            }
            if (newest != cursor) local.setCursor(table, cookbookId, newest)
        }
    }

    private suspend fun uploadPhotos(cookbookId: String): List<Exception> {
        val failures = mutableListOf<Exception>()
        for (table in PhotoTables) {
            for (photo in local.photosToUpload(table)) {
                if (photo.cookbookId != cookbookId) continue
                try {
                    val full = photos.read(checkNotNull(photo.localPath))
                    val thumb = photo.thumbnailPath?.let { runCatching { photos.read(it) }.getOrNull() } ?: full
                    val path = "$cookbookId/${photo.id}.jpg"
                    val thumbPath = "$cookbookId/thumbs/${photo.id}.jpg"
                    remote.upload(path, full)
                    remote.upload(thumbPath, thumb)
                    local.setStoragePaths(table, photo.id, path, thumbPath)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: FileNotFoundException) {
                    // The file is gone from this phone; nothing to upload, don't retry forever.
                } catch (e: Exception) {
                    failures += e // the row stays held back; this photo is retried next pass
                }
            }
        }
        return failures
    }

    private suspend fun downloadPhotos(cookbookId: String): List<Exception> {
        val failures = mutableListOf<Exception>()
        for (table in PhotoTables) {
            for (photo in local.photosToDownload(table)) {
                if (photo.cookbookId != cookbookId) continue
                try {
                    val full = remote.download(checkNotNull(photo.storagePath))
                    val thumb = photo.thumbnailStoragePath?.let { remote.download(it) } ?: full
                    val (fullPath, thumbPath) = photos.saveDownloaded(photo.id, full, thumb)
                    local.setLocalPaths(table, photo.id, fullPath, thumbPath)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failures += e
                }
            }
        }
        return failures
    }

    private fun kotlinx.serialization.json.JsonObject.cookbookOf(table: SyncTable<*>): String? =
        (this[if (table.name == "cookbooks") "id" else "cookbook_id"] as? JsonPrimitive)?.content

    private companion object {
        val PhotoTables = listOf("photos", "cook_log_photos")
        const val PushBatch = 200
        const val PullPage = 500
        const val PullOverlapMs = 60_000L
        const val MaxPushRounds = 3
    }
}
