package app.jscookbook.ui.common

import app.jscookbook.core.designsystem.recipeimage.RecipeImageRequest
import app.jscookbook.core.model.Recipe
import app.jscookbook.core.model.RecipeSummary

/** Tiles use the small thumbnail. */
fun RecipeSummary.imageRequest() = RecipeImageRequest(
    title = title,
    type = type,
    categoryNames = categoryNames,
    imageSource = imageSource,
    coverPhoto = coverThumbnailPath,
)

/** The detail hero starts from the same thumbnail as the tile, so the transition is seamless. */
fun Recipe.thumbnailImageRequest() = RecipeImageRequest(
    title = title,
    type = type,
    categoryNames = categories.map { it.name },
    imageSource = imageSource,
    coverPhoto = cover?.thumbnailPath,
)
