package app.jscookbook.core.model

enum class RecipeType(val label: String) {
    Breakfast("Breakfast"),
    Meal("Meal"),
    Snack("Snack"),
    Side("Side"),
    Dessert("Dessert"),
    Drink("Drink"),
    Sauce("Sauce"),
}

/** Stored per recipe: where its tile image comes from. */
enum class ImageSource { PHOTO, FALLBACK, AI }

data class Category(
    val id: String,
    val name: String,
    /** A key into the curated icon set (the food illustrations). */
    val iconKey: String,
    /** A key into the curated palette colors. */
    val colorKey: String,
    val position: Int,
    val recipeCount: Int = 0,
)

/** What a tile or list row needs. */
data class RecipeSummary(
    val id: String,
    val title: String,
    val type: RecipeType?,
    val isFavorite: Boolean,
    val rating: Int?,
    val totalMinutes: Int?,
    val coverThumbnailPath: String?,
    val imageSource: ImageSource,
    val categoryNames: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
)

data class Recipe(
    val id: String,
    val title: String,
    val type: RecipeType?,
    val description: String,
    val servings: String,
    val prepMinutes: Int?,
    val cookMinutes: Int?,
    val sourceUrl: String,
    val tags: List<String>,
    val isFavorite: Boolean,
    val rating: Int?,
    val notes: String,
    val imageSource: ImageSource,
    val categories: List<Category>,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val photos: List<Photo>,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val totalMinutes: Int? get() = totalMinutes(prepMinutes, cookMinutes)
    val cover: Photo? get() = photos.firstOrNull { it.isCover } ?: photos.firstOrNull()
}

data class Ingredient(
    val id: String,
    /** Heading this ingredient sits under, e.g. "For the sauce". */
    val section: String?,
    val quantity: String?,
    val unit: String?,
    val name: String,
    val note: String?,
)

data class Step(
    val id: String,
    val text: String,
    val timerSeconds: Int?,
)

data class Photo(
    val id: String,
    val localPath: String?,
    val thumbnailPath: String?,
    val width: Int,
    val height: Int,
    val isCover: Boolean,
    val caption: String,
)

fun totalMinutes(prep: Int?, cook: Int?): Int? =
    if (prep == null && cook == null) null else (prep ?: 0) + (cook ?: 0)

/** "45 min", "1 hr", "1 hr 15 min". */
fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest min"
        rest == 0 -> "$hours hr"
        else -> "$hours hr $rest min"
    }
}

/** "1:05", "12:00", "1:02:03". */
fun formatCountdown(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

/**
 * Everything the editor saves. Child rows carry ids generated on the device (UUIDs), so a new
 * recipe is complete and syncable the moment it's saved, even offline.
 */
data class RecipeDraft(
    val id: String,
    val title: String = "",
    val type: RecipeType? = null,
    val description: String = "",
    val servings: String = "",
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val sourceUrl: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val rating: Int? = null,
    val notes: String = "",
    val categoryIds: Set<String> = emptySet(),
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
    val photos: List<Photo> = emptyList(),
)

fun Recipe.toDraft() = RecipeDraft(
    id = id,
    title = title,
    type = type,
    description = description,
    servings = servings,
    prepMinutes = prepMinutes,
    cookMinutes = cookMinutes,
    sourceUrl = sourceUrl,
    tags = tags,
    isFavorite = isFavorite,
    rating = rating,
    notes = notes,
    categoryIds = categories.map { it.id }.toSet(),
    ingredients = ingredients,
    steps = steps,
    photos = photos,
)
