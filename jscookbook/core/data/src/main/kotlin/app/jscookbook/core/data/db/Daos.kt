package app.jscookbook.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** A recipe row plus what a tile needs from other tables. */
data class RecipeSummaryRow(
    val id: String,
    val title: String,
    val type: String?,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    val rating: Int?,
    @ColumnInfo(name = "prep_minutes") val prepMinutes: Int?,
    @ColumnInfo(name = "cook_minutes") val cookMinutes: Int?,
    @ColumnInfo(name = "image_source") val imageSource: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "cover_thumbnail") val coverThumbnail: String?,
    /** Category names joined with the unit separator (char 31). */
    @ColumnInfo(name = "category_names") val categoryNames: String?,
)

data class CategoryWithCountRow(
    val id: String,
    val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "color_key") val colorKey: String,
    val position: Int,
    @ColumnInfo(name = "recipe_count") val recipeCount: Int,
)

private const val SUMMARY_COLUMNS = """
    r.id, r.title, r.type, r.is_favorite, r.rating, r.prep_minutes, r.cook_minutes, r.image_source, r.created_at, r.updated_at,
    (SELECT p.thumbnail_path FROM photos p
        WHERE p.recipe_id = r.id AND p.deleted_at IS NULL
        ORDER BY p.is_cover DESC, p.position ASC LIMIT 1) AS cover_thumbnail,
    (SELECT group_concat(c.name, char(31)) FROM recipe_categories rc
        JOIN categories c ON c.id = rc.category_id
        WHERE rc.recipe_id = r.id AND rc.deleted_at IS NULL AND c.deleted_at IS NULL) AS category_names
"""

@Dao
interface CookbookDao {
    @Query("SELECT * FROM cookbooks WHERE deleted_at IS NULL ORDER BY created_at LIMIT 1")
    suspend fun first(): CookbookEntity?

    @Upsert
    suspend fun upsert(cookbook: CookbookEntity)
}

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT c.id, c.name, c.icon_key, c.color_key, c.position,
            (SELECT count(*) FROM recipe_categories rc JOIN recipes r ON r.id = rc.recipe_id
                WHERE rc.category_id = c.id AND rc.deleted_at IS NULL AND r.deleted_at IS NULL) AS recipe_count
        FROM categories c
        WHERE c.cookbook_id = :cookbookId AND c.deleted_at IS NULL
        ORDER BY c.position, c.created_at
        """,
    )
    fun observeWithCounts(cookbookId: String): Flow<List<CategoryWithCountRow>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun get(id: String): CategoryEntity?

    @Query("SELECT coalesce(max(position), -1) FROM categories WHERE cookbook_id = :cookbookId AND deleted_at IS NULL")
    suspend fun maxPosition(cookbookId: String): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("UPDATE categories SET deleted_at = :now, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("UPDATE categories SET deleted_at = NULL, updated_at = :now WHERE id = :id")
    suspend fun restore(id: String, now: Long)
}

@Dao
interface RecipeDao {
    @Query("SELECT $SUMMARY_COLUMNS FROM recipes r WHERE r.cookbook_id = :cookbookId AND r.deleted_at IS NULL ORDER BY r.updated_at DESC")
    fun observeSummaries(cookbookId: String): Flow<List<RecipeSummaryRow>>

    @Query(
        """
        SELECT $SUMMARY_COLUMNS FROM recipes r
        WHERE r.deleted_at IS NULL AND r.id IN
            (SELECT recipe_id FROM recipe_categories WHERE category_id = :categoryId AND deleted_at IS NULL)
        ORDER BY r.updated_at DESC
        """,
    )
    fun observeSummariesInCategory(categoryId: String): Flow<List<RecipeSummaryRow>>

    @Query("SELECT * FROM recipes WHERE id = :id AND deleted_at IS NULL")
    fun observe(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun get(id: String): RecipeEntity?

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)

    @Query("UPDATE recipes SET is_favorite = :favorite, updated_at = :now WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, now: Long)

    @Query("UPDATE recipes SET deleted_at = :now, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("UPDATE recipes SET deleted_at = NULL, updated_at = :now WHERE id = :id")
    suspend fun restore(id: String, now: Long)
}

@Dao
interface RecipeChildrenDao {
    @Query(
        """
        SELECT c.* FROM categories c JOIN recipe_categories rc ON rc.category_id = c.id
        WHERE rc.recipe_id = :recipeId AND rc.deleted_at IS NULL AND c.deleted_at IS NULL
        ORDER BY c.position
        """,
    )
    fun observeCategories(recipeId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId AND deleted_at IS NULL ORDER BY position")
    fun observeIngredients(recipeId: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM steps WHERE recipe_id = :recipeId AND deleted_at IS NULL ORDER BY position")
    fun observeSteps(recipeId: String): Flow<List<StepEntity>>

    @Query("SELECT * FROM photos WHERE recipe_id = :recipeId AND deleted_at IS NULL ORDER BY position")
    fun observePhotos(recipeId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM recipe_categories WHERE recipe_id = :recipeId")
    suspend fun allCategoryLinks(recipeId: String): List<RecipeCategoryEntity>

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun allIngredients(recipeId: String): List<IngredientEntity>

    @Query("SELECT * FROM steps WHERE recipe_id = :recipeId")
    suspend fun allSteps(recipeId: String): List<StepEntity>

    @Query("SELECT * FROM photos WHERE recipe_id = :recipeId")
    suspend fun allPhotos(recipeId: String): List<PhotoEntity>

    @Upsert suspend fun upsertCategoryLinks(links: List<RecipeCategoryEntity>)

    @Upsert suspend fun upsertIngredients(rows: List<IngredientEntity>)

    @Upsert suspend fun upsertSteps(rows: List<StepEntity>)

    @Upsert suspend fun upsertPhotos(rows: List<PhotoEntity>)
}
