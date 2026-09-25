package app.jscookbook.core.designsystem.recipeimage

import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackArtSelectorTest {

    @Test
    fun sameInputAlwaysGetsSameArt() {
        val first = FallbackArtSelector.select("Grandma's Tomato Soup", RecipeType.Meal, listOf("Soups"))
        repeat(200) {
            assertEquals(first, FallbackArtSelector.select("Grandma's Tomato Soup", RecipeType.Meal, listOf("Soups")))
        }
    }

    @Test
    fun titleIsNormalizedBeforeHashing() {
        val tidy = FallbackArtSelector.select("tomato soup", RecipeType.Meal)
        val messy = FallbackArtSelector.select("  Tomato \t  SOUP ", RecipeType.Meal)
        assertEquals(tidy.tint, messy.tint)
        assertEquals(tidy.illustration, messy.illustration)
        assertEquals(tidy.tiltDegrees, messy.tiltDegrees, 0f)
    }

    @Test
    fun hashMatchesFnv1aReferenceVectors() {
        assertEquals(0x811C9DC5.toInt(), FallbackArtSelector.fnv1a32(""))
        assertEquals(0xE40C292C.toInt(), FallbackArtSelector.fnv1a32("a"))
        assertEquals(0xBF9CF968.toInt(), FallbackArtSelector.fnv1a32("foobar"))
    }

    /**
     * Golden values computed independently (Python FNV-1a over UTF-8). If this fails, every
     * existing recipe would change color and illustration on upgrade; that's a breaking change.
     */
    @Test
    fun goldenArtIsStableAcrossVersions() {
        fun check(title: String, type: RecipeType, tint: String, illustration: FoodIllustration) {
            val art = FallbackArtSelector.select(title, type)
            assertEquals("tint for $title", tint, art.tint.name)
            assertEquals("illustration for $title", illustration, art.illustration)
        }
        check("Banana Bread", RecipeType.Dessert, "Georgetown Pink Beige", FoodIllustration.Loaf)
        check("Tomato Soup", RecipeType.Meal, "Cinnamon", FoodIllustration.Pot)
        check("Iced Chai", RecipeType.Drink, "Venetian Portico", FoodIllustration.Cup)
        check("Shakshuka", RecipeType.Breakfast, "Cinnamon", FoodIllustration.Skillet)
    }

    @Test
    fun illustrationPrefersTitleThenCategoryThenType() {
        val fromTitle = FallbackArtSelector.illustrationsFor("Chicken noodle soup", RecipeType.Meal, listOf("Baking"))
        assertEquals(listOf(FoodIllustration.Pot, FoodIllustration.Bowl), fromTitle)

        val fromCategory = FallbackArtSelector.illustrationsFor("Weeknight thing", RecipeType.Meal, listOf("Baking"))
        assertEquals(listOf(FoodIllustration.Loaf), fromCategory)

        val fromType = FallbackArtSelector.illustrationsFor("Mystery", RecipeType.Drink, listOf("Favorites"))
        assertEquals(listOf(FoodIllustration.Glass, FoodIllustration.Cup), fromType)

        val anything = FallbackArtSelector.illustrationsFor("Mystery", null, emptyList())
        assertEquals(FoodIllustration.entries, anything)
    }

    @Test
    fun keywordsMatchWholeWordsNotFragments() {
        // "tea" must not match inside "steak"; "sweet" potatoes are not dessert.
        val steak = FallbackArtSelector.illustrationsFor("Steak frites", RecipeType.Meal, emptyList())
        assertTrue(FoodIllustration.Cup !in steak)
        val potatoes = FallbackArtSelector.illustrationsFor("Sweet potato fries", RecipeType.Side, emptyList())
        assertEquals(listOf(FoodIllustration.Bowl, FoodIllustration.Plate), potatoes)
        // Plurals and explicit prefixes still match.
        assertEquals(listOf(FoodIllustration.Cookie), FallbackArtSelector.illustrationsFor("Oatmeal cookies", null, emptyList()))
        assertEquals(listOf(FoodIllustration.Loaf), FallbackArtSelector.illustrationsFor("Baked goods", null, emptyList()))
        assertEquals(listOf(FoodIllustration.Pot, FoodIllustration.Bowl), FallbackArtSelector.illustrationsFor("Two curries", null, emptyList()))
    }

    @Test
    fun initialIsFirstLetterOrDigit() {
        assertEquals("B", FallbackArtSelector.initialOf("  banana bread"))
        assertEquals("B", FallbackArtSelector.initialOf("🍌 bread"))
        assertEquals("3", FallbackArtSelector.initialOf("3-bean chili"))
        assertEquals("É", FallbackArtSelector.initialOf("éclairs"))
        assertEquals("", FallbackArtSelector.initialOf("   "))
    }

    @Test
    fun tintsSpreadAcrossThePalette() {
        val titles = listOf(
            "Tomato Soup", "Banana Bread", "Iced Chai", "Shakshuka", "Lemon Vinaigrette", "Roast Chicken",
            "Green Beans", "Chocolate Chip Cookies", "Mac and Cheese", "Chili", "Pancakes", "Pesto Pasta",
            "Fish Tacos", "Apple Crisp", "Caesar Salad", "Guacamole", "Lasagna", "Pad Thai", "Burrito Bowl",
            "French Toast", "Meatballs", "Risotto", "Hummus", "Cornbread", "Pot Roast", "Minestrone",
            "Brownies", "Lemonade", "Granola", "Fried Rice",
        )
        val tints = titles.map { FallbackArtSelector.select(it, RecipeType.Meal).tint }.toSet()
        assertTrue("only ${tints.size} tints used", tints.size >= 7)
    }

    @Test
    fun tiltStaysSubtle() {
        listOf("a", "b", "Tomato Soup", "zzz", "").forEach {
            val tilt = FallbackArtSelector.select(it, null).tiltDegrees
            assertTrue(tilt in -8f..8f)
        }
    }

    @Test
    fun providerUsesCoverPhotoWhenThereIsOne() {
        val image = DefaultRecipeImageProvider.imageFor(
            RecipeImageRequest("Tomato Soup", imageSource = ImageSource.PHOTO, coverPhoto = "file://cover.jpg"),
        )
        assertEquals(RecipeImage.Photo("file://cover.jpg"), image)
    }

    @Test
    fun providerFallsBackWhenNothingElseExists() {
        val noPhoto = DefaultRecipeImageProvider.imageFor(RecipeImageRequest("Tomato Soup", RecipeType.Meal))
        assertEquals(ImageSource.FALLBACK, noPhoto.source)

        val aiNotGenerated = DefaultRecipeImageProvider.imageFor(
            RecipeImageRequest("Tomato Soup", RecipeType.Meal, imageSource = ImageSource.AI),
        )
        assertEquals(ImageSource.FALLBACK, aiNotGenerated.source)
        assertNotEquals(ImageSource.AI, aiNotGenerated.source)
    }

    @Test
    fun fallbackInitialsAreReadableAsLargeText() {
        FallbackTints.all.forEach {
            val ratio = contrastRatio(it.ink, it.container)
            assertTrue("${it.name} is ${"%.2f".format(ratio)}:1", ratio >= 3f)
        }
    }

    @Test
    fun cardPaletteTextPassesAa() {
        CardPalettes.all.forEach {
            val ratio = contrastRatio(it.content, it.container)
            assertTrue("${it.name} is ${"%.2f".format(ratio)}:1", ratio >= 4.5f)
        }
    }
}
