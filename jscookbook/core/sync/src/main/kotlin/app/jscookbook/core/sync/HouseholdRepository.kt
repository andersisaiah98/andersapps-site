package app.jscookbook.core.sync

import app.jscookbook.core.data.db.CookbookDao
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.repository.CurrentCookbook
import app.jscookbook.core.data.sync.LocalSync
import app.jscookbook.core.data.sync.SyncJson
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The household: one shared cook book that both phones belong to. One phone starts sharing (its
 * cook book goes up with the same id), makes an invite code, and the other phone joins with it.
 */
@Singleton
class HouseholdRepository @Inject constructor(
    private val backend: Backend,
    private val local: LocalSync,
    private val cookbook: CurrentCookbook,
    private val cookbooks: CookbookDao,
    private val clock: Clock,
    private val scheduler: SyncScheduler,
) {
    /** The shared cook book's id, or null while this phone isn't in a household. */
    fun observeLinked(): Flow<String?> = local.observeLinkedCookbook()

    /** How many people are in the household (from the last sync). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeMemberCount(): Flow<Int> = local.observeLinkedCookbook().flatMapLatest { id ->
        if (id == null) flowOf(0) else cookbooks.observeMemberCount(id)
    }

    /** Publishes this phone's cook book and starts syncing it. Safe to call again. */
    suspend fun startSharing() {
        val id = cookbook.id()
        val row = cookbooks.get(id)
        client().postgrest.rpc(
            "create_cookbook",
            buildJsonObject {
                put("p_id", id)
                put("p_name", row?.name ?: "J's Cook Book")
                put("p_created_at", row?.createdAt ?: clock.now())
            },
        )
        local.linkCurrentCookbook()
        scheduler.syncNow()
    }

    /** A single-use code for the other phone, valid for 7 days. */
    suspend fun createInvite(): String {
        val id = checkNotNull(local.linkedCookbook()) { "Start sharing first" }
        return client().postgrest.rpc("create_invite", buildJsonObject { put("p_cookbook_id", id) }).decodeAs<String>()
    }

    /** Joins the household behind [code]. Recipes already on this phone move into it. */
    suspend fun join(code: String) {
        val id = client().postgrest.rpc("join_cookbook", buildJsonObject { put("p_code", code.trim()) }).decodeAs<String>()
        local.adoptCookbook(id)
        scheduler.syncNow()
    }

    /**
     * After signing in again on a phone that was already in the household, picks the link back up
     * without asking. Returns true when it did.
     */
    suspend fun relinkIfMember(userId: String): Boolean {
        val id = cookbook.id()
        val result = client().from("cookbook_members").select {
            filter {
                eq("cookbook_id", id)
                eq("user_id", userId)
            }
        }
        val isMember = SyncJson.parseToJsonElement(result.data).jsonArray.isNotEmpty()
        if (isMember) local.linkCurrentCookbook()
        return isMember
    }

    private fun client() = checkNotNull(backend.client) { "Sync isn't set up in this build" }
}
