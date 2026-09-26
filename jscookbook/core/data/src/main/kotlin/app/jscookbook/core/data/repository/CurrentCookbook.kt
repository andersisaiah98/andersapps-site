package app.jscookbook.core.data.repository

import app.jscookbook.core.data.db.CookbookDao
import app.jscookbook.core.data.db.CookbookEntity
import app.jscookbook.core.data.db.SyncDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
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
 * The cook book everything on this phone belongs to. Until the household is set up it's a local
 * one created on first launch; starting to share publishes it with the same id, and joining the
 * other phone's household switches to that cook book (see LocalSync.adoptCookbook).
 */
@Singleton
class CurrentCookbook @Inject constructor(
    private val dao: CookbookDao,
    private val syncDao: SyncDao,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    private val current = MutableStateFlow<String?>(null)

    suspend fun id(): String = current.value ?: mutex.withLock {
        current.value ?: (syncDao.state(LinkedCookbookKey) ?: dao.first()?.id ?: create()).also { current.value = it }
    }

    /** The current id, and every later one (it changes when this phone joins a household). */
    fun observeId(): Flow<String> = flow {
        id()
        emitAll(current.filterNotNull())
    }.distinctUntilChanged()

    internal fun switchTo(id: String) {
        current.value = id
    }

    private suspend fun create(): String {
        val now = clock.now()
        val cookbook = CookbookEntity(id = newId(), name = "J's Cook Book", createdAt = now, updatedAt = now)
        dao.upsert(cookbook)
        return cookbook.id
    }

    companion object {
        /** sync_state key holding the id of the shared cook book once this phone is in a household. */
        const val LinkedCookbookKey = "linked_cookbook"
    }
}
