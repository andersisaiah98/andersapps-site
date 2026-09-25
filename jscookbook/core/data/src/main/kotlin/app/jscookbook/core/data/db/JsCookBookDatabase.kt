package app.jscookbook.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
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
    ],
)
abstract class JsCookBookDatabase : RoomDatabase() {
    abstract fun cookbookDao(): CookbookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeChildrenDao(): RecipeChildrenDao
}
