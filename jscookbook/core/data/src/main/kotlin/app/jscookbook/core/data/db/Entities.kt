package app.jscookbook.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// These mirror the Postgres tables that sync uses in Phase 2 (supabase/migrations/). Every row has
// a UUID id, created_at / updated_at (epoch millis), a nullable deleted_at for soft delete, and the
// cookbook_id it belongs to. Nothing is ever hard-deleted locally before it has synced.

@Entity(tableName = "cookbooks")
data class CookbookEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "cookbook_members", indices = [Index("cookbook_id")])
data class CookbookMemberEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val role: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "categories", indices = [Index("cookbook_id")])
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "color_key") val colorKey: String,
    val position: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "recipes", indices = [Index("cookbook_id")])
data class RecipeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val title: String,
    /** A RecipeType name, or null. */
    val type: String?,
    val description: String,
    val servings: String,
    @ColumnInfo(name = "prep_minutes") val prepMinutes: Int?,
    @ColumnInfo(name = "cook_minutes") val cookMinutes: Int?,
    @ColumnInfo(name = "source_url") val sourceUrl: String,
    /** Newline-separated. */
    val tags: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    val rating: Int?,
    val notes: String,
    /** An ImageSource name. */
    @ColumnInfo(name = "image_source") val imageSource: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(
    tableName = "recipe_categories",
    primaryKeys = ["recipe_id", "category_id"],
    indices = [Index("category_id"), Index("cookbook_id")],
)
data class RecipeCategoryEntity(
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "ingredients", indices = [Index("recipe_id"), Index("cookbook_id")])
data class IngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val position: Int,
    val section: String?,
    val quantity: String?,
    val unit: String?,
    val name: String,
    val note: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "steps", indices = [Index("recipe_id"), Index("cookbook_id")])
data class StepEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val position: Int,
    val text: String,
    @ColumnInfo(name = "timer_seconds") val timerSeconds: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "photos", indices = [Index("recipe_id"), Index("cookbook_id")])
data class PhotoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    /** Path in the Supabase Storage bucket once uploaded (Phase 2). */
    @ColumnInfo(name = "storage_path") val storagePath: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String?,
    val width: Int,
    val height: Int,
    @ColumnInfo(name = "is_cover") val isCover: Boolean,
    val caption: String,
    val position: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "cook_logs", indices = [Index("recipe_id"), Index("cookbook_id")])
data class CookLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    /** The day it was made, as an epoch day. */
    @ColumnInfo(name = "made_on") val madeOn: Long,
    val rating: Int?,
    val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)

@Entity(tableName = "cook_log_photos", indices = [Index("cook_log_id"), Index("cookbook_id")])
data class CookLogPhotoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cook_log_id") val cookLogId: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @ColumnInfo(name = "storage_path") val storagePath: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String?,
    val width: Int,
    val height: Int,
    val caption: String,
    val position: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
)
