package app.jscookbook.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = 2,
    exportSchema = true,
    entities = [
        CookbookEntity::class,
        CookbookMemberEntity::class,
        CategoryEntity::class,
        RecipeEntity::class,
        RecipeCategoryEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        PhotoEntity::class,
        CookLogEntity::class,
        CookLogPhotoEntity::class,
        SyncStateEntity::class,
    ],
)
abstract class JsCookBookDatabase : RoomDatabase() {
    abstract fun cookbookDao(): CookbookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeChildrenDao(): RecipeChildrenDao
    abstract fun syncDao(): SyncDao

    companion object {
        /** Every table that syncs, in the order pushes must go (parents before children). */
        val SyncedTables = listOf(
            "cookbooks", "cookbook_members", "categories", "recipes", "recipe_categories",
            "ingredients", "steps", "photos", "cook_logs", "cook_log_photos",
        )
    }
}

/**
 * Phase 2 adds sync: a `dirty` flag on every row (1 = changed since it last synced; everything that
 * already exists is unsynced), the Storage path of photo thumbnails, and the sync_state table.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        JsCookBookDatabase.SyncedTables.forEach { table ->
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `dirty` INTEGER NOT NULL DEFAULT 1")
        }
        db.execSQL("ALTER TABLE `photos` ADD COLUMN `thumbnail_storage_path` TEXT")
        db.execSQL("ALTER TABLE `cook_log_photos` ADD COLUMN `thumbnail_storage_path` TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_state` (`name` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`name`))")
    }
}
