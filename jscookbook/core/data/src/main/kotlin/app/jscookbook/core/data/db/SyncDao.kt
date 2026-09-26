package app.jscookbook.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

/** What last-write-wins needs to know about a local row. */
data class RowVersion(
    val key: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val dirty: Boolean,
)

/** A photo's files, on this phone and in Storage. */
data class PhotoFileRow(
    val id: String,
    @ColumnInfo(name = "cookbook_id") val cookbookId: String,
    @ColumnInfo(name = "storage_path") val storagePath: String?,
    @ColumnInfo(name = "thumbnail_storage_path") val thumbnailStoragePath: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String?,
)

/** Reads and writes used only by the sync engine. Screens never call this. */
@Dao
interface SyncDao {
    // Rows changed on this phone since they last synced.
    @Query("SELECT * FROM cookbooks WHERE dirty = 1") suspend fun dirtyCookbooks(): List<CookbookEntity>
    @Query("SELECT * FROM categories WHERE dirty = 1") suspend fun dirtyCategories(): List<CategoryEntity>
    @Query("SELECT * FROM recipes WHERE dirty = 1") suspend fun dirtyRecipes(): List<RecipeEntity>
    @Query("SELECT * FROM recipe_categories WHERE dirty = 1") suspend fun dirtyRecipeCategories(): List<RecipeCategoryEntity>
    @Query("SELECT * FROM ingredients WHERE dirty = 1") suspend fun dirtyIngredients(): List<IngredientEntity>
    @Query("SELECT * FROM steps WHERE dirty = 1") suspend fun dirtySteps(): List<StepEntity>
    @Query("SELECT * FROM photos WHERE dirty = 1") suspend fun dirtyPhotos(): List<PhotoEntity>
    @Query("SELECT * FROM cook_logs WHERE dirty = 1") suspend fun dirtyCookLogs(): List<CookLogEntity>
    @Query("SELECT * FROM cook_log_photos WHERE dirty = 1") suspend fun dirtyCookLogPhotos(): List<CookLogPhotoEntity>

    @Query(
        """
        SELECT (SELECT count(*) FROM cookbooks WHERE dirty = 1)
            + (SELECT count(*) FROM categories WHERE dirty = 1)
            + (SELECT count(*) FROM recipes WHERE dirty = 1)
            + (SELECT count(*) FROM recipe_categories WHERE dirty = 1)
            + (SELECT count(*) FROM ingredients WHERE dirty = 1)
            + (SELECT count(*) FROM steps WHERE dirty = 1)
            + (SELECT count(*) FROM photos WHERE dirty = 1)
            + (SELECT count(*) FROM cook_logs WHERE dirty = 1)
            + (SELECT count(*) FROM cook_log_photos WHERE dirty = 1)
        """,
    )
    fun observePendingCount(): Flow<Int>

    // Rows arriving from the server.
    @Upsert suspend fun upsertCookbooks(rows: List<CookbookEntity>)
    @Upsert suspend fun upsertMembers(rows: List<CookbookMemberEntity>)
    @Upsert suspend fun upsertCategories(rows: List<CategoryEntity>)
    @Upsert suspend fun upsertRecipes(rows: List<RecipeEntity>)
    @Upsert suspend fun upsertRecipeCategories(rows: List<RecipeCategoryEntity>)
    @Upsert suspend fun upsertIngredients(rows: List<IngredientEntity>)
    @Upsert suspend fun upsertSteps(rows: List<StepEntity>)
    @Upsert suspend fun upsertPhotos(rows: List<PhotoEntity>)
    @Upsert suspend fun upsertCookLogs(rows: List<CookLogEntity>)
    @Upsert suspend fun upsertCookLogPhotos(rows: List<CookLogPhotoEntity>)

    @Query("SELECT * FROM photos WHERE id IN (:ids)") suspend fun photos(ids: List<String>): List<PhotoEntity>
    @Query("SELECT * FROM cook_log_photos WHERE id IN (:ids)") suspend fun cookLogPhotos(ids: List<String>): List<CookLogPhotoEntity>

    /** Use [versionsQuery] to build the query. */
    @RawQuery suspend fun versions(query: SupportSQLiteQuery): List<RowVersion>

    @RawQuery suspend fun photoFiles(query: SupportSQLiteQuery): List<PhotoFileRow>

    @RawQuery suspend fun count(query: SupportSQLiteQuery): Int

    // Sync state.
    @Query("SELECT value FROM sync_state WHERE name = :name") suspend fun state(name: String): String?
    @Query("SELECT value FROM sync_state WHERE name = :name") fun observeState(name: String): Flow<String?>
    @Upsert suspend fun putState(state: SyncStateEntity)
    @Query("DELETE FROM sync_state WHERE name LIKE :pattern") suspend fun clearState(pattern: String)

    companion object {
        /** SQL for a table's row key: the id, or "recipe_id:category_id" for the link table. */
        fun keyExpression(table: String): String =
            if (table == "recipe_categories") "recipe_id || ':' || category_id" else "id"

        fun versionsQuery(table: String, keys: List<String>): SupportSQLiteQuery {
            require(table in JsCookBookDatabase.SyncedTables)
            val key = keyExpression(table)
            val marks = keys.joinToString(",") { "?" }
            return SimpleSQLiteQuery(
                "SELECT $key AS `key`, updated_at, dirty FROM `$table` WHERE $key IN ($marks)",
                keys.toTypedArray(),
            )
        }

        private const val PhotoColumns = "id, cookbook_id, storage_path, thumbnail_storage_path, local_path, thumbnail_path"

        /** Photos that are on this phone but not yet in Storage. */
        fun photosToUploadQuery(table: String): SupportSQLiteQuery = SimpleSQLiteQuery(
            "SELECT $PhotoColumns FROM `${photoTable(table)}` " +
                "WHERE storage_path IS NULL AND local_path IS NOT NULL AND deleted_at IS NULL",
        )

        /** Photos that are in Storage (added on the other phone) but not yet on this one. */
        fun photosToDownloadQuery(table: String): SupportSQLiteQuery = SimpleSQLiteQuery(
            "SELECT $PhotoColumns FROM `${photoTable(table)}` " +
                "WHERE storage_path IS NOT NULL AND local_path IS NULL AND deleted_at IS NULL",
        )

        fun photoTable(table: String): String {
            require(table == "photos" || table == "cook_log_photos")
            return table
        }
    }
}
