package app.jscookbook.core.data.sync

import androidx.room.withTransaction
import app.jscookbook.core.data.db.CookbookEntity
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.db.PhotoFileRow
import app.jscookbook.core.data.db.SyncDao
import app.jscookbook.core.data.db.SyncStateEntity
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.repository.CurrentCookbook
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/** A local row ready to push: its key and updated_at (to mark it clean afterwards) and its JSON. */
class PushRow(val key: String, val updatedAt: Long, val json: JsonObject)

/**
 * The phone side of sync: which rows need pushing, applying rows pulled from the server with
 * last-write-wins, pull cursors, and switching cook books when joining a household. It knows
 * nothing about the network, so it's tested directly against Room.
 */
@Singleton
class LocalSync @Inject constructor(
    private val db: JsCookBookDatabase,
    private val cookbook: CurrentCookbook,
    private val clock: Clock,
) {
    private val dao: SyncDao = db.syncDao()

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    suspend fun pendingPush(table: SyncTable<*>): List<PushRow> = pendingPushTyped(table)

    private suspend fun <E : Any> pendingPushTyped(table: SyncTable<E>): List<PushRow> {
        if (!table.pushable) return emptyList()
        return table.dirtyRows(dao)
            .filterNot(table.holdBack)
            .map { PushRow(table.key(it), table.updatedAt(it), SyncJson.encodeToJsonElement(table.serializer, it).jsonObject) }
    }

    /**
     * Marks pushed rows clean, but only where updated_at still matches what was sent: a row edited
     * again while the push was in flight stays dirty and goes out next time.
     */
    suspend fun markPushed(table: SyncTable<*>, rows: List<PushRow>) {
        if (rows.isEmpty()) return
        val key = SyncDao.keyExpression(table.name)
        db.withTransaction {
            val writer = db.openHelper.writableDatabase
            rows.forEach { row ->
                writer.execSQL(
                    "UPDATE `${table.name}` SET dirty = 0 WHERE $key = ? AND updated_at = ?",
                    arrayOf<Any>(row.key, row.updatedAt),
                )
            }
        }
    }

    /** Applies rows pulled from the server. Returns how many changed this phone's data. */
    suspend fun applyRemote(table: SyncTable<*>, rows: List<JsonObject>): Int = applyRemoteTyped(table, rows)

    private suspend fun <E : Any> applyRemoteTyped(table: SyncTable<E>, rows: List<JsonObject>): Int {
        if (rows.isEmpty()) return 0
        val incoming = rows.map { SyncJson.decodeFromJsonElement(table.serializer, it) }
        return db.withTransaction {
            val local = incoming.map(table.key).chunked(500)
                .flatMap { dao.versions(SyncDao.versionsQuery(table.name, it)) }
                .associateBy { it.key }
            val winners = incoming.filter { row ->
                val mine = local[table.key(row)]
                shouldApplyRemote(mine?.updatedAt, mine?.dirty ?: false, table.updatedAt(row))
            }.map(table.synced)
            if (winners.isNotEmpty()) table.upsert(dao, table.keepLocalFields(dao, winners))
            winners.size
        }
    }

    // --- Cursors: the highest server_updated_at seen per table, per cook book. ---

    suspend fun cursor(table: SyncTable<*>, cookbookId: String): Long =
        dao.state(cursorKey(table, cookbookId))?.toLongOrNull() ?: 0L

    suspend fun setCursor(table: SyncTable<*>, cookbookId: String, value: Long) =
        dao.putState(SyncStateEntity(cursorKey(table, cookbookId), value.toString()))

    private fun cursorKey(table: SyncTable<*>, cookbookId: String) = "cursor:${table.name}:$cookbookId"

    // --- Household link and status. ---

    suspend fun linkedCookbook(): String? = dao.state(CurrentCookbook.LinkedCookbookKey)

    fun observeLinkedCookbook(): Flow<String?> = dao.observeState(CurrentCookbook.LinkedCookbookKey)

    /** This phone's own cook book is now on the server (same id); sync it from here on. */
    suspend fun linkCurrentCookbook(): String {
        val id = cookbook.id()
        dao.putState(SyncStateEntity(CurrentCookbook.LinkedCookbookKey, id))
        return id
    }

    /**
     * Joins the household's cook book [newId]: everything made on this phone so far moves into it
     * (so nothing is lost) and is pushed on the next sync. The old, never-shared local cook book
     * row is removed. The shared one's real name arrives with the first pull.
     */
    suspend fun adoptCookbook(newId: String) {
        val oldId = cookbook.id()
        if (oldId != newId) {
            db.withTransaction {
                val writer = db.openHelper.writableDatabase
                JsCookBookDatabase.SyncedTables.filter { it != "cookbooks" }.forEach { table ->
                    writer.execSQL("UPDATE `$table` SET cookbook_id = ?, dirty = 1 WHERE cookbook_id = ?", arrayOf(newId, oldId))
                }
                writer.execSQL("DELETE FROM cookbooks WHERE id = ?", arrayOf(oldId))
                // Placeholder until the pull brings the real row; updated_at 0 so the server's wins.
                dao.upsertCookbooks(
                    listOf(CookbookEntity(id = newId, name = "J's Cook Book", createdAt = clock.now(), updatedAt = 0, dirty = false)),
                )
                dao.putState(SyncStateEntity(CurrentCookbook.LinkedCookbookKey, newId))
            }
        } else {
            dao.putState(SyncStateEntity(CurrentCookbook.LinkedCookbookKey, newId))
        }
        cookbook.switchTo(newId)
    }

    /** Stops syncing (sign out). Local data stays on the phone. */
    suspend fun unlink() {
        db.withTransaction {
            dao.clearState(CurrentCookbook.LinkedCookbookKey)
            dao.clearState("cursor:%")
        }
    }

    suspend fun lastSyncedAt(): Long? = dao.state(LastSyncKey)?.toLongOrNull()

    fun observeLastSyncedAt(): Flow<String?> = dao.observeState(LastSyncKey)

    suspend fun setLastSyncedAt(time: Long) = dao.putState(SyncStateEntity(LastSyncKey, time.toString()))

    // --- Photo files. ---

    suspend fun photosToUpload(table: String): List<PhotoFileRow> = dao.photoFiles(SyncDao.photosToUploadQuery(table))

    suspend fun photosToDownload(table: String): List<PhotoFileRow> = dao.photoFiles(SyncDao.photosToDownloadQuery(table))

    /** Records where a photo was uploaded. The row stays dirty so the paths get pushed. */
    suspend fun setStoragePaths(table: String, id: String, storagePath: String, thumbnailStoragePath: String) =
        exec(
            "UPDATE `${SyncDao.photoTable(table)}` SET storage_path = ?, thumbnail_storage_path = ? WHERE id = ?",
            arrayOf(storagePath, thumbnailStoragePath, id),
        )

    /** Records where a downloaded photo was saved. Phone-only, so the row's sync state is untouched. */
    suspend fun setLocalPaths(table: String, id: String, localPath: String, thumbnailPath: String) =
        exec(
            "UPDATE `${SyncDao.photoTable(table)}` SET local_path = ?, thumbnail_path = ? WHERE id = ?",
            arrayOf(localPath, thumbnailPath, id),
        )

    private suspend fun exec(sql: String, args: Array<out Any?>) {
        db.withTransaction { db.openHelper.writableDatabase.execSQL(sql, args) }
    }

    companion object {
        const val LastSyncKey = "last_sync"
    }
}

/**
 * JSON as Postgres sees it: nulls and defaults are always written (so clearing deleted_at on a
 * restore actually reaches the server) and unknown server columns are ignored.
 */
val SyncJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = true
    coerceInputValues = true
}
