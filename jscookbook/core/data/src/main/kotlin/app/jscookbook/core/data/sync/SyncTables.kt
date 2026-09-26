package app.jscookbook.core.data.sync

import app.jscookbook.core.data.db.CategoryEntity
import app.jscookbook.core.data.db.CookLogEntity
import app.jscookbook.core.data.db.CookLogPhotoEntity
import app.jscookbook.core.data.db.CookbookEntity
import app.jscookbook.core.data.db.CookbookMemberEntity
import app.jscookbook.core.data.db.IngredientEntity
import app.jscookbook.core.data.db.PhotoEntity
import app.jscookbook.core.data.db.RecipeCategoryEntity
import app.jscookbook.core.data.db.RecipeEntity
import app.jscookbook.core.data.db.StepEntity
import app.jscookbook.core.data.db.SyncDao
import kotlinx.serialization.KSerializer

/** How one Room table maps onto its Postgres twin. */
class SyncTable<E : Any>(
    val name: String,
    val serializer: KSerializer<E>,
    /** Postgres conflict target for upserts. */
    val conflictColumns: String = "id",
    /** False for cookbook_members, which only the server writes. */
    val pushable: Boolean = true,
    val key: (E) -> String,
    val updatedAt: (E) -> Long,
    val dirtyRows: suspend SyncDao.() -> List<E>,
    val upsert: suspend SyncDao.(List<E>) -> Unit,
    /** The row marked as synced (dirty = false). */
    val synced: (E) -> E,
    /** Carries phone-only fields (photo file paths) from local rows onto incoming ones. */
    val keepLocalFields: suspend SyncDao.(List<E>) -> List<E> = { it },
    /** A dirty row that must wait (a photo whose file hasn't uploaded yet). */
    val holdBack: (E) -> Boolean = { false },
)

object SyncTables {

    val cookbooks = SyncTable<CookbookEntity>(
        name = "cookbooks", serializer = CookbookEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyCookbooks() }, upsert = { upsertCookbooks(it) }, synced = { it.copy(dirty = false) },
    )

    val members = SyncTable<CookbookMemberEntity>(
        name = "cookbook_members", serializer = CookbookMemberEntity.serializer(), pushable = false,
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { emptyList() }, upsert = { upsertMembers(it) }, synced = { it.copy(dirty = false) },
    )

    val categories = SyncTable<CategoryEntity>(
        name = "categories", serializer = CategoryEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyCategories() }, upsert = { upsertCategories(it) }, synced = { it.copy(dirty = false) },
    )

    val recipes = SyncTable<RecipeEntity>(
        name = "recipes", serializer = RecipeEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyRecipes() }, upsert = { upsertRecipes(it) }, synced = { it.copy(dirty = false) },
    )

    val recipeCategories = SyncTable<RecipeCategoryEntity>(
        name = "recipe_categories", serializer = RecipeCategoryEntity.serializer(),
        conflictColumns = "recipe_id,category_id",
        key = { "${it.recipeId}:${it.categoryId}" }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyRecipeCategories() }, upsert = { upsertRecipeCategories(it) }, synced = { it.copy(dirty = false) },
    )

    val ingredients = SyncTable<IngredientEntity>(
        name = "ingredients", serializer = IngredientEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyIngredients() }, upsert = { upsertIngredients(it) }, synced = { it.copy(dirty = false) },
    )

    val steps = SyncTable<StepEntity>(
        name = "steps", serializer = StepEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtySteps() }, upsert = { upsertSteps(it) }, synced = { it.copy(dirty = false) },
    )

    val photos = SyncTable<PhotoEntity>(
        name = "photos", serializer = PhotoEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyPhotos() }, upsert = { upsertPhotos(it) }, synced = { it.copy(dirty = false) },
        keepLocalFields = { rows ->
            val local = photos(rows.map { it.id }).associateBy { it.id }
            rows.map { row ->
                val mine = local[row.id]
                if (mine != null && mine.storagePath == row.storagePath) {
                    row.copy(localPath = mine.localPath, thumbnailPath = mine.thumbnailPath)
                } else {
                    row
                }
            }
        },
        holdBack = { it.deletedAt == null && it.storagePath == null && it.localPath != null },
    )

    val cookLogs = SyncTable<CookLogEntity>(
        name = "cook_logs", serializer = CookLogEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyCookLogs() }, upsert = { upsertCookLogs(it) }, synced = { it.copy(dirty = false) },
    )

    val cookLogPhotos = SyncTable<CookLogPhotoEntity>(
        name = "cook_log_photos", serializer = CookLogPhotoEntity.serializer(),
        key = { it.id }, updatedAt = { it.updatedAt },
        dirtyRows = { dirtyCookLogPhotos() }, upsert = { upsertCookLogPhotos(it) }, synced = { it.copy(dirty = false) },
        keepLocalFields = { rows ->
            val local = cookLogPhotos(rows.map { it.id }).associateBy { it.id }
            rows.map { row ->
                val mine = local[row.id]
                if (mine != null && mine.storagePath == row.storagePath) {
                    row.copy(localPath = mine.localPath, thumbnailPath = mine.thumbnailPath)
                } else {
                    row
                }
            }
        },
        holdBack = { it.deletedAt == null && it.storagePath == null && it.localPath != null },
    )

    /** Parents before children, so pushes never reference a row the server hasn't seen. */
    val all: List<SyncTable<*>> = listOf(
        cookbooks, members, categories, recipes, recipeCategories, ingredients, steps, photos, cookLogs, cookLogPhotos,
    )

    fun named(name: String): SyncTable<*> = all.first { it.name == name }
}
