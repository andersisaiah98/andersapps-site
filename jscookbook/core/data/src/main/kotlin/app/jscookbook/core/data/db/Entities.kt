package app.jscookbook.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// These mirror the Postgres tables in supabase/migrations/. Every row has a UUID id, created_at /
// updated_at (epoch millis), a nullable deleted_at for soft delete, and the cookbook_id it belongs
// to. Nothing is ever hard-deleted.
//
// Each entity is also the JSON shape sync sends and receives (@SerialName = the Postgres column).
// @Transient fields are phone-only: `dirty` marks a row changed since it last synced (every local
// write leaves it at the default, true), and photo file paths are where this phone keeps its copy.

@Serializable
@Entity(tableName = "cookbooks")
data class CookbookEntity(
    @PrimaryKey val id: String,
    val name: String,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "cookbook_members", indices = [Index("cookbook_id")])
data class CookbookMemberEntity(
    @PrimaryKey val id: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @SerialName("user_id") @ColumnInfo(name = "user_id") val userId: String,
    val role: String,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "categories", indices = [Index("cookbook_id")])
data class CategoryEntity(
    @PrimaryKey val id: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val name: String,
    @SerialName("icon_key") @ColumnInfo(name = "icon_key") val iconKey: String,
    @SerialName("color_key") @ColumnInfo(name = "color_key") val colorKey: String,
    val position: Int,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "recipes", indices = [Index("cookbook_id")])
data class RecipeEntity(
    @PrimaryKey val id: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val title: String,
    /** A RecipeType name, or null. */
    val type: String?,
    val description: String,
    val servings: String,
    @SerialName("prep_minutes") @ColumnInfo(name = "prep_minutes") val prepMinutes: Int?,
    @SerialName("cook_minutes") @ColumnInfo(name = "cook_minutes") val cookMinutes: Int?,
    @SerialName("source_url") @ColumnInfo(name = "source_url") val sourceUrl: String,
    /** Newline-separated. */
    val tags: String,
    @SerialName("is_favorite") @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    val rating: Int?,
    val notes: String,
    /** An ImageSource name. */
    @SerialName("image_source") @ColumnInfo(name = "image_source") val imageSource: String,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(
    tableName = "recipe_categories",
    primaryKeys = ["recipe_id", "category_id"],
    indices = [Index("category_id"), Index("cookbook_id")],
)
data class RecipeCategoryEntity(
    @SerialName("recipe_id") @ColumnInfo(name = "recipe_id") val recipeId: String,
    @SerialName("category_id") @ColumnInfo(name = "category_id") val categoryId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "ingredients", indices = [Index("recipe_id"), Index("cookbook_id")])
data class IngredientEntity(
    @PrimaryKey val id: String,
    @SerialName("recipe_id") @ColumnInfo(name = "recipe_id") val recipeId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val position: Int,
    val section: String?,
    val quantity: String?,
    val unit: String?,
    val name: String,
    val note: String?,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "steps", indices = [Index("recipe_id"), Index("cookbook_id")])
data class StepEntity(
    @PrimaryKey val id: String,
    @SerialName("recipe_id") @ColumnInfo(name = "recipe_id") val recipeId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    val position: Int,
    val text: String,
    @SerialName("timer_seconds") @ColumnInfo(name = "timer_seconds") val timerSeconds: Int?,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "photos", indices = [Index("recipe_id"), Index("cookbook_id")])
data class PhotoEntity(
    @PrimaryKey val id: String,
    @SerialName("recipe_id") @ColumnInfo(name = "recipe_id") val recipeId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    /** Paths in the Supabase Storage bucket once uploaded; null until then. */
    @SerialName("storage_path") @ColumnInfo(name = "storage_path") val storagePath: String?,
    @SerialName("thumbnail_storage_path") @ColumnInfo(name = "thumbnail_storage_path") val thumbnailStoragePath: String? = null,
    @Transient @ColumnInfo(name = "local_path") val localPath: String? = null,
    @Transient @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    val width: Int,
    val height: Int,
    @SerialName("is_cover") @ColumnInfo(name = "is_cover") val isCover: Boolean,
    val caption: String,
    val position: Int,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "cook_logs", indices = [Index("recipe_id"), Index("cookbook_id")])
data class CookLogEntity(
    @PrimaryKey val id: String,
    @SerialName("recipe_id") @ColumnInfo(name = "recipe_id") val recipeId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    /** The day it was made, as an epoch day. */
    @SerialName("made_on") @ColumnInfo(name = "made_on") val madeOn: Long,
    val rating: Int?,
    val notes: String,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

@Serializable
@Entity(tableName = "cook_log_photos", indices = [Index("cook_log_id"), Index("cookbook_id")])
data class CookLogPhotoEntity(
    @PrimaryKey val id: String,
    @SerialName("cook_log_id") @ColumnInfo(name = "cook_log_id") val cookLogId: String,
    @SerialName("cookbook_id") @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @SerialName("storage_path") @ColumnInfo(name = "storage_path") val storagePath: String?,
    @SerialName("thumbnail_storage_path") @ColumnInfo(name = "thumbnail_storage_path") val thumbnailStoragePath: String? = null,
    @Transient @ColumnInfo(name = "local_path") val localPath: String? = null,
    @Transient @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    val width: Int,
    val height: Int,
    val caption: String,
    val position: Int,
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long,
    @SerialName("updated_at") @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @SerialName("deleted_at") @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @Transient @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

/** Small key/value state for sync: per-table pull cursors, the linked cook book, last sync time. */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val name: String,
    val value: String,
)
