package app.jscookbook.core.data.repository

import app.jscookbook.core.data.db.CategoryDao
import app.jscookbook.core.data.db.CategoryEntity
import app.jscookbook.core.model.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
    private val cookbook: CurrentCookbook,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCategories(): Flow<List<Category>> =
        flow { emit(cookbook.id()) }
            .flatMapLatest { dao.observeWithCounts(it) }
            .map { rows -> rows.map { it.toModel() } }

    /** Creates a category when [id] is null, otherwise renames / re-styles it. Returns its id. */
    suspend fun save(id: String?, name: String, iconKey: String, colorKey: String): String {
        val now = clock.now()
        val cookbookId = cookbook.id()
        val existing = id?.let { dao.get(it) }
        val entity = existing?.copy(name = name.trim(), iconKey = iconKey, colorKey = colorKey, updatedAt = now)
            ?: CategoryEntity(
                id = id ?: newId(),
                cookbookId = cookbookId,
                name = name.trim(),
                iconKey = iconKey,
                colorKey = colorKey,
                position = dao.maxPosition(cookbookId) + 1,
                createdAt = now,
                updatedAt = now,
            )
        dao.upsert(entity)
        return entity.id
    }

    suspend fun delete(id: String) = dao.softDelete(id, clock.now())

    suspend fun restore(id: String) = dao.restore(id, clock.now())
}
