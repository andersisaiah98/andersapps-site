package app.jscookbook.core.data.repository

import androidx.room.withTransaction
import app.jscookbook.core.data.db.IngredientEntity
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.db.PhotoEntity
import app.jscookbook.core.data.db.RecipeCategoryEntity
import app.jscookbook.core.data.db.RecipeEntity
import app.jscookbook.core.data.db.StepEntity
import app.jscookbook.core.model.ImageSource
import app.jscookbook.core.model.Recipe
import app.jscookbook.core.model.RecipeDraft
import app.jscookbook.core.model.RecipeSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Recipes and their ingredients, steps, photos and categories. Room is the source of truth. */
@Singleton
class RecipeRepository @Inject constructor(
    private val db: JsCookBookDatabase,
    private val cookbook: CurrentCookbook,
    private val clock: Clock,
) {
    private val recipes = db.recipeDao()
    private val children = db.recipeChildrenDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecipes(): Flow<List<RecipeSummary>> =
        flow { emit(cookbook.id()) }
            .flatMapLatest { recipes.observeSummaries(it) }
            .map { rows -> rows.map { it.toModel() } }

    fun observeRecipesInCategory(categoryId: String): Flow<List<RecipeSummary>> =
        recipes.observeSummariesInCategory(categoryId).map { rows -> rows.map { it.toModel() } }

    fun observeRecipe(id: String): Flow<Recipe?> = combine(
        recipes.observe(id),
        children.observeCategories(id),
        children.observeIngredients(id),
        children.observeSteps(id),
        children.observePhotos(id),
    ) { recipe, categories, ingredients, steps, photos ->
        recipe?.let {
            Recipe(
                id = it.id,
                title = it.title,
                type = it.type.toRecipeType(),
                description = it.description,
                servings = it.servings,
                prepMinutes = it.prepMinutes,
                cookMinutes = it.cookMinutes,
                sourceUrl = it.sourceUrl,
                tags = it.tags.toTags(),
                isFavorite = it.isFavorite,
                rating = it.rating,
                notes = it.notes,
                imageSource = it.imageSource.toImageSource(),
                categories = categories.map { c -> c.toModel() },
                ingredients = ingredients.map { i -> i.toModel() },
                steps = steps.map { s -> s.toModel() },
                photos = photos.map { p -> p.toModel() },
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }.distinctUntilChanged()

    /**
     * Saves the whole recipe in one transaction. Children that were removed in the editor are
     * soft-deleted (so the deletion can sync); everything else is upserted in its new order.
     */
    suspend fun save(draft: RecipeDraft) {
        val now = clock.now()
        val cookbookId = cookbook.id()
        db.withTransaction {
            val existing = recipes.get(draft.id)
            recipes.upsert(
                RecipeEntity(
                    id = draft.id,
                    cookbookId = existing?.cookbookId ?: cookbookId,
                    title = draft.title.trim(),
                    type = draft.type?.name,
                    description = draft.description.trim(),
                    servings = draft.servings.trim(),
                    prepMinutes = draft.prepMinutes,
                    cookMinutes = draft.cookMinutes,
                    sourceUrl = draft.sourceUrl.trim(),
                    tags = draft.tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n"),
                    isFavorite = draft.isFavorite,
                    rating = draft.rating,
                    notes = draft.notes.trim(),
                    imageSource = (if (draft.photos.isNotEmpty()) ImageSource.PHOTO else ImageSource.FALLBACK).name,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    deletedAt = null,
                ),
            )
            val owner = existing?.cookbookId ?: cookbookId

            val links = children.allCategoryLinks(draft.id).associateBy { it.categoryId }
            children.upsertCategoryLinks(
                (links.keys + draft.categoryIds).map { categoryId ->
                    val link = links[categoryId]
                    val keep = categoryId in draft.categoryIds
                    when {
                        link == null -> RecipeCategoryEntity(draft.id, categoryId, owner, now, now)
                        keep && link.deletedAt == null -> link
                        keep -> link.copy(deletedAt = null, updatedAt = now)
                        link.deletedAt == null -> link.copy(deletedAt = now, updatedAt = now)
                        else -> link
                    }
                },
            )

            val oldIngredients = children.allIngredients(draft.id).associateBy { it.id }
            children.upsertIngredients(
                draft.ingredients.mapIndexed { index, item ->
                    IngredientEntity(
                        id = item.id, recipeId = draft.id, cookbookId = owner, position = index,
                        section = item.section?.trim()?.ifEmpty { null }, quantity = item.quantity?.trim()?.ifEmpty { null },
                        unit = item.unit?.trim()?.ifEmpty { null }, name = item.name.trim(), note = item.note?.trim()?.ifEmpty { null },
                        createdAt = oldIngredients[item.id]?.createdAt ?: now, updatedAt = now,
                    )
                } + removed(oldIngredients.values, draft.ingredients.map { it.id }.toSet(), { it.id }, { it.deletedAt }) {
                    it.copy(deletedAt = now, updatedAt = now)
                },
            )

            val oldSteps = children.allSteps(draft.id).associateBy { it.id }
            children.upsertSteps(
                draft.steps.mapIndexed { index, step ->
                    StepEntity(
                        id = step.id, recipeId = draft.id, cookbookId = owner, position = index,
                        text = step.text.trim(), timerSeconds = step.timerSeconds?.takeIf { it > 0 },
                        createdAt = oldSteps[step.id]?.createdAt ?: now, updatedAt = now,
                    )
                } + removed(oldSteps.values, draft.steps.map { it.id }.toSet(), { it.id }, { it.deletedAt }) {
                    it.copy(deletedAt = now, updatedAt = now)
                },
            )

            val oldPhotos = children.allPhotos(draft.id).associateBy { it.id }
            val coverId = draft.photos.firstOrNull { it.isCover }?.id ?: draft.photos.firstOrNull()?.id
            children.upsertPhotos(
                draft.photos.mapIndexed { index, photo ->
                    val old = oldPhotos[photo.id]
                    PhotoEntity(
                        id = photo.id, recipeId = draft.id, cookbookId = owner,
                        storagePath = old?.storagePath, localPath = photo.localPath, thumbnailPath = photo.thumbnailPath,
                        width = photo.width, height = photo.height, isCover = photo.id == coverId,
                        caption = photo.caption.trim(), position = index,
                        createdAt = old?.createdAt ?: now, updatedAt = now,
                    )
                } + removed(oldPhotos.values, draft.photos.map { it.id }.toSet(), { it.id }, { it.deletedAt }) {
                    it.copy(deletedAt = now, updatedAt = now)
                },
            )
        }
    }

    suspend fun setFavorite(id: String, favorite: Boolean) = recipes.setFavorite(id, favorite, clock.now())

    /** Soft delete; [restore] undoes it (the snackbar's Undo). */
    suspend fun delete(id: String) = recipes.softDelete(id, clock.now())

    suspend fun restore(id: String) = recipes.restore(id, clock.now())

    private inline fun <T> removed(
        old: Collection<T>,
        keep: Set<String>,
        id: (T) -> String,
        deletedAt: (T) -> Long?,
        markDeleted: (T) -> T,
    ): List<T> = old.filter { id(it) !in keep && deletedAt(it) == null }.map(markDeleted)
}
