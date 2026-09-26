package app.jscookbook.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.repository.CategoryRepository
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.repository.CurrentCookbook
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.data.repository.newId
import app.jscookbook.core.model.ImageSource
import app.jscookbook.core.model.Ingredient
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.RecipeDraft
import app.jscookbook.core.model.RecipeType
import app.jscookbook.core.model.Step
import app.jscookbook.core.model.toDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecipeRepositoryTest {

    private lateinit var db: JsCookBookDatabase
    private var now = 1_000L
    private val clock = Clock { now }
    private lateinit var recipes: RecipeRepository
    private lateinit var categories: CategoryRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), JsCookBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val cookbook = CurrentCookbook(db.cookbookDao(), db.syncDao(), clock)
        recipes = RecipeRepository(db, cookbook, clock)
        categories = CategoryRepository(db.categoryDao(), cookbook, clock)
    }

    @After
    fun tearDown() = db.close()

    private fun draft(id: String = newId(), categoryIds: Set<String> = emptySet()) = RecipeDraft(
        id = id,
        title = "  Tomato Soup ",
        type = RecipeType.Meal,
        servings = "4",
        prepMinutes = 10,
        cookMinutes = 30,
        tags = listOf("cozy", "", "cozy", "weeknight"),
        categoryIds = categoryIds,
        ingredients = listOf(
            Ingredient("i1", null, "2", "tbsp", "olive oil", null),
            Ingredient("i2", "For the soup", "1", "can", "tomatoes", "14 oz"),
        ),
        steps = listOf(Step("s1", "Warm the oil.", null), Step("s2", "Simmer.", 900)),
    )

    @Test
    fun savedRecipeRoundTrips() = runTest {
        val soups = categories.save(null, "Soups", "Pot", "terracotta")
        val id = newId()
        recipes.save(draft(id, setOf(soups)))

        val recipe = recipes.observeRecipe(id).first()!!
        assertEquals("Tomato Soup", recipe.title)
        assertEquals(40, recipe.totalMinutes)
        assertEquals(listOf("cozy", "weeknight"), recipe.tags)
        assertEquals(listOf("i1", "i2"), recipe.ingredients.map { it.id })
        assertEquals("For the soup", recipe.ingredients[1].section)
        assertEquals(900, recipe.steps[1].timerSeconds)
        assertEquals(listOf("Soups"), recipe.categories.map { it.name })
        assertEquals(ImageSource.FALLBACK, recipe.imageSource)

        val summary = recipes.observeRecipes().first().single()
        assertEquals(listOf("Soups"), summary.categoryNames)
        assertEquals(1, categories.observeCategories().first().single().recipeCount)
    }

    @Test
    fun removedChildrenAreSoftDeletedAndOrderIsKept() = runTest {
        val id = newId()
        recipes.save(draft(id))
        now = 2_000L
        val edited = recipes.observeRecipe(id).first()!!.toDraft().copy(
            ingredients = listOf(Ingredient("i2", null, "1", "can", "tomatoes", null), Ingredient("i3", null, null, null, "basil", null)),
            steps = listOf(Step("s2", "Simmer.", null)),
        )
        recipes.save(edited)

        val recipe = recipes.observeRecipe(id).first()!!
        assertEquals(listOf("i2", "i3"), recipe.ingredients.map { it.id })
        assertEquals(listOf("s2"), recipe.steps.map { it.id })
        // The removed rows are still there, marked deleted, so the deletion can sync.
        val removed = db.recipeChildrenDao().allIngredients(id).single { it.id == "i1" }
        assertEquals(2_000L, removed.deletedAt)
        assertEquals(2_000L, removed.updatedAt)
    }

    @Test
    fun coverIsFirstPhotoWhenNoneIsMarked() = runTest {
        val id = newId()
        recipes.save(
            draft(id).copy(
                photos = listOf(
                    Photo("p1", "/full/1.jpg", "/thumb/1.jpg", 2048, 1536, false, ""),
                    Photo("p2", "/full/2.jpg", "/thumb/2.jpg", 2048, 1536, false, "Plated"),
                ),
            ),
        )
        val recipe = recipes.observeRecipe(id).first()!!
        assertEquals("p1", recipe.cover?.id)
        assertEquals(ImageSource.PHOTO, recipe.imageSource)
        assertEquals("/thumb/1.jpg", recipes.observeRecipes().first().single().coverThumbnailPath)
    }

    @Test
    fun deleteHidesRecipeAndRestoreBringsItBack() = runTest {
        val id = newId()
        recipes.save(draft(id))
        recipes.delete(id)
        assertNull(recipes.observeRecipe(id).first())
        assertTrue(recipes.observeRecipes().first().isEmpty())

        recipes.restore(id)
        assertNotNull(recipes.observeRecipe(id).first())
    }

    @Test
    fun favoritesAndCategoryLinksUpdate() = runTest {
        val a = categories.save(null, "Soups", "Pot", "terracotta")
        val b = categories.save(null, "Weeknight", "Skillet", "portico")
        val id = newId()
        recipes.save(draft(id, setOf(a)))
        recipes.setFavorite(id, true)
        recipes.save(recipes.observeRecipe(id).first()!!.toDraft().copy(categoryIds = setOf(b)))

        val recipe = recipes.observeRecipe(id).first()!!
        assertTrue(recipe.isFavorite)
        assertEquals(listOf("Weeknight"), recipe.categories.map { it.name })
        val counts = categories.observeCategories().first().associate { it.name to it.recipeCount }
        assertEquals(mapOf("Soups" to 0, "Weeknight" to 1), counts)
    }

    @Test
    fun categoriesAppendInOrderAndDeleteSoftly() = runTest {
        val a = categories.save(null, "Soups", "Pot", "terracotta")
        categories.save(null, "Baking", "Loaf", "pink_beige")
        assertEquals(listOf("Soups", "Baking"), categories.observeCategories().first().map { it.name })
        categories.delete(a)
        assertEquals(listOf("Baking"), categories.observeCategories().first().map { it.name })
        categories.restore(a)
        assertFalse(categories.observeCategories().first().none { it.id == a })
    }
}
