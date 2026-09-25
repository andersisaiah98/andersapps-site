package app.jscookbook.core.designsystem.recipeimage

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** Stored per recipe: where its tile image comes from. */
enum class ImageSource { PHOTO, FALLBACK, AI }

enum class RecipeType(val label: String) {
    Breakfast("Breakfast"),
    Meal("Meal"),
    Snack("Snack"),
    Side("Side"),
    Dessert("Dessert"),
    Drink("Drink"),
    Sauce("Sauce"),
}

/** What a tile knows about its recipe when it asks for an image. */
@Immutable
data class RecipeImageRequest(
    val title: String,
    val type: RecipeType? = null,
    val categoryNames: List<String> = emptyList(),
    val imageSource: ImageSource = ImageSource.FALLBACK,
    /** The cover photo: a local file or signed URL, whatever the image loader accepts. */
    val coverPhoto: Any? = null,
    /** Reserved for a future AI image provider. Nothing fills this in v1. */
    val generatedImage: Any? = null,
)

sealed interface RecipeImage {
    val source: ImageSource

    data class Photo(val model: Any) : RecipeImage {
        override val source: ImageSource get() = ImageSource.PHOTO
    }

    data class Generated(val model: Any) : RecipeImage {
        override val source: ImageSource get() = ImageSource.AI
    }

    data class Fallback(val art: FallbackArt) : RecipeImage {
        override val source: ImageSource get() = ImageSource.FALLBACK
    }
}

/** Decides what a recipe tile shows. Swap the implementation to add AI images later. */
fun interface RecipeImageProvider {
    fun imageFor(request: RecipeImageRequest): RecipeImage
}

/**
 * v1: the cover photo if there is one, otherwise generated fallback art. An AI image is used only
 * when the recipe is marked [ImageSource.AI] and one exists, which never happens yet.
 */
object DefaultRecipeImageProvider : RecipeImageProvider {
    override fun imageFor(request: RecipeImageRequest): RecipeImage {
        request.coverPhoto?.let { return RecipeImage.Photo(it) }
        if (request.imageSource == ImageSource.AI) {
            request.generatedImage?.let { return RecipeImage.Generated(it) }
        }
        return RecipeImage.Fallback(
            FallbackArtSelector.select(request.title, request.type, request.categoryNames),
        )
    }
}

val LocalRecipeImageProvider = staticCompositionLocalOf<RecipeImageProvider> { DefaultRecipeImageProvider }
