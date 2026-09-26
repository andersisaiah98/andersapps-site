package app.jscookbook.core.sync

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import app.jscookbook.core.data.sync.SyncJson
import javax.inject.Inject
import javax.inject.Singleton

/** The server side of sync, kept narrow so the engine can be tested against an in-memory server. */
interface SyncRemote {
    /** False when sync isn't configured or nobody is signed in. */
    fun isReady(): Boolean

    /** Inserts or updates rows. The server keeps whichever version has the newer updated_at. */
    suspend fun upsert(table: String, conflictColumns: String, rows: List<JsonObject>)

    /** Rows of [cookbookId] with server_updated_at >= [since], oldest change first. */
    suspend fun fetchSince(table: String, cookbookId: String, since: Long, offset: Long, limit: Int): List<JsonObject>

    suspend fun upload(path: String, bytes: ByteArray)

    suspend fun download(path: String): ByteArray
}

@Singleton
class SupabaseSyncRemote @Inject constructor(private val backend: Backend) : SyncRemote {

    private val client get() = checkNotNull(backend.client) { "Sync isn't configured" }

    override fun isReady(): Boolean = backend.client?.auth?.currentSessionOrNull() != null

    override suspend fun upsert(table: String, conflictColumns: String, rows: List<JsonObject>) {
        if (rows.isEmpty()) return
        client.from(table).upsert(JsonArray(rows)) { onConflict = conflictColumns }
    }

    override suspend fun fetchSince(table: String, cookbookId: String, since: Long, offset: Long, limit: Int): List<JsonObject> {
        val result = client.from(table).select {
            filter {
                eq("cookbook_id", cookbookId)
                gte("server_updated_at", since)
            }
            order("server_updated_at", Order.ASCENDING)
            range(offset, offset + limit - 1)
        }
        return SyncJson.parseToJsonElement(result.data).jsonArray.map { it.jsonObject }
    }

    override suspend fun upload(path: String, bytes: ByteArray) {
        // upsert: a retried upload after a dropped connection simply overwrites the partial attempt.
        client.storage.from(Backend.PhotoBucket).upload(path, bytes) { upsert = true }
    }

    override suspend fun download(path: String): ByteArray =
        client.storage.from(Backend.PhotoBucket).downloadAuthenticated(path)
}
