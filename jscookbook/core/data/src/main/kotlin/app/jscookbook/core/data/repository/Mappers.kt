package app.jscookbook.core.data.repository

import app.jscookbook.core.data.db.CategoryEntity
import app.jscookbook.core.data.db.CategoryWithCountRow
import app.jscookbook.core.data.db.IngredientEntity
import app.jscookbook.core.data.db.PhotoEntity
import app.jscookbook.core.data.db.RecipeSummaryRow
import app.jscookbook.core.data.db.StepEntity
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.ImageSource
import app.jscookbook.core.model.Ingredient
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.RecipeSummary
import app.jscookbook.core.model.RecipeType
import app.jscookbook.core.model.Step
import app.jscookbook.core.model.totalMinutes

internal fun String?.toRecipeType(): RecipeType? = this?.let { name -> RecipeType.entries.firstOrNull { it.name == name } }

internal fun String.toImageSource(): ImageSource = ImageSource.entries.firstOrNull { it.name == this } ?: ImageSource.FALLBACK

internal fun String.toTags(): List<String> = split('\n').map { it.trim() }.filter { it.isNotEmpty() }

internal fun RecipeSummaryRow.toModel() = RecipeSummary(
    id = id,
    title = title,
    type = type.toRecipeType(),
    isFavorite = isFavorite,
    rating = rating,
    totalMinutes = totalMinutes(prepMinutes, cookMinutes),
    coverThumbnailPath = coverThumbnail,
    imageSource = imageSource.toImageSource(),
    categoryNames = categoryNames?.split('\u001F')?.filter { it.isNotEmpty() }.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun CategoryWithCountRow.toModel() = Category(id, name, iconKey, colorKey, position, recipeCount)

internal fun CategoryEntity.toModel() = Category(id, name, iconKey, colorKey, position)

internal fun IngredientEntity.toModel() = Ingredient(id, section, quantity, unit, name, note)

internal fun StepEntity.toModel() = Step(id, text, timerSeconds)

internal fun PhotoEntity.toModel() = Photo(id, localPath, thumbnailPath, width, height, isCover, caption)
