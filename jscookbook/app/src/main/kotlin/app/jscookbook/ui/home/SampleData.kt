package app.jscookbook.ui.home

import app.jscookbook.core.designsystem.component.CardPalette
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.RecipeImageRequest
import app.jscookbook.core.designsystem.recipeimage.RecipeType

// Phase 0 preview content so the design can be judged on real screens. Room replaces this in Phase 1.

data class SampleRecipe(
    val title: String,
    val type: RecipeType,
    val categories: List<String>,
    val note: String,
    val favorite: Boolean,
) {
    val imageRequest: RecipeImageRequest get() = RecipeImageRequest(title, type, categories)
}

val SampleRecipes = listOf(
    SampleRecipe("Tomato Soup", RecipeType.Meal, listOf("Soups & stews"), "Made 2 days ago", favorite = true),
    SampleRecipe("Banana Bread", RecipeType.Dessert, listOf("Baking"), "Made last week", favorite = true),
    SampleRecipe("Shakshuka", RecipeType.Breakfast, listOf("Weekend"), "Made 9 days ago", favorite = false),
    SampleRecipe("Lemon Vinaigrette", RecipeType.Sauce, listOf("Staples"), "Made 12 days ago", favorite = false),
    SampleRecipe("Chocolate Chip Cookies", RecipeType.Snack, listOf("Baking"), "Made 2 weeks ago", favorite = true),
    SampleRecipe("Iced Chai", RecipeType.Drink, listOf("Drinks"), "Made 3 weeks ago", favorite = true),
    SampleRecipe("Roast Chicken & Potatoes", RecipeType.Meal, listOf("Sunday"), "Made last month", favorite = true),
    SampleRecipe("Garlicky Green Beans", RecipeType.Side, listOf("Weeknight"), "Made last month", favorite = false),
)

data class SampleCategory(
    val name: String,
    val illustration: FoodIllustration,
    val palette: CardPalette,
    val count: Int,
)

val SampleCategories = listOf(
    SampleCategory("Weeknight", FoodIllustration.Skillet, CardPalettes.TerraCotta, 14),
    SampleCategory("Soups & stews", FoodIllustration.Pot, CardPalettes.PinkBeige, 9),
    SampleCategory("Baking", FoodIllustration.Loaf, CardPalettes.MexicanTile, 11),
    SampleCategory("Breakfast", FoodIllustration.Egg, CardPalettes.Portico, 7),
    SampleCategory("Sweet things", FoodIllustration.Cupcake, CardPalettes.Cinnamon, 12),
    SampleCategory("Drinks", FoodIllustration.Glass, CardPalettes.Edgecomb, 5),
    SampleCategory("Sauces & staples", FoodIllustration.Jar, CardPalettes.Rustique, 8),
    SampleCategory("Salads", FoodIllustration.Bowl, CardPalettes.PinkBeige, 6),
)
