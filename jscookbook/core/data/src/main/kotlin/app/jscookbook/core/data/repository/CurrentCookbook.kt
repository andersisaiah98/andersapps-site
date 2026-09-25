package app.jscookbook.core.data.repository

import app.jscookbook.core.data.db.CookbookDao
import app.jscookbook.core.data.db.CookbookEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

fun interface Clock {
    fun now(): Long
}

fun newId(): String = UUID.randomUUID().toString()

/**
 * The cook book everything on this phone belongs to. Before sign-in (Phase 2) it's a local one
 * created on first launch; joining the household adopts the shared cook book's id.
 */
@Singleton
class CurrentCookbook @Inject constructor(
    private val dao: CookbookDao,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    @Volatile private var cached: String? = null

    suspend fun id(): String = cached ?: mutex.withLock {
        cached ?: (dao.first()?.id ?: create()).also { cached = it }
    }

    private suspend fun create(): String {
        val now = clock.now()
        val cookbook = CookbookEntity(id = newId(), name = "J's Cook Book", createdAt = now, updatedAt = now)
        dao.upsert(cookbook)
        return cookbook.id
    }
}
