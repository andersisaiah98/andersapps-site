package app.jscookbook.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.component.JsIconButton
import app.jscookbook.core.designsystem.component.JsSegmentedControl
import app.jscookbook.core.designsystem.component.QuickActionCard
import app.jscookbook.core.designsystem.component.RecipeTile
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.DefaultRecipeImageProvider
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.RecipeImageRequest
import app.jscookbook.core.model.RecipeType
import app.jscookbook.core.designsystem.theme.JsTheme

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.buttonSection() {
    catalogHeader("buttons", "Buttons", "Pills with a springy press. Every one clears 48 dp for touch.")
    item(key = "buttons") {
        JsCard {
            FlowRow(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JsButton("Let's make it", onClick = {})
                JsButton("Reroll", onClick = {}, style = JsButtonStyle.Tonal, icon = JsIcons.Dice)
                JsButton("Outline", onClick = {}, style = JsButtonStyle.Outline)
                JsButton("Text", onClick = {}, style = JsButtonStyle.Text)
                JsButton("Disabled", onClick = {}, enabled = false)
                JsIconButton(JsIcons.Heart, contentDescription = "Favorite", onClick = {}, tint = JsTheme.extendedColors.brandText)
                JsIconButton(JsIcons.Search, contentDescription = "Search", onClick = {})
            }
        }
    }
    item(key = "segmented") {
        var selected by remember { mutableIntStateOf(0) }
        JsSegmentedControl(
            options = listOf("System", "Light", "Dark"),
            selectedIndex = selected,
            onSelect = { selected = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal fun LazyListScope.cardSection() {
    catalogHeader("cards", "Cards & tiles", "Quick actions use palettes whose text passes AA; tiles lead with the image.")
    item(key = "quick-large") {
        QuickActionCard(
            title = "Give me a random meal to make",
            subtitle = "Skips anything from the last week",
            onClick = {},
            illustration = FoodIllustration.Plate,
            palette = CardPalettes.TerraCotta,
            icon = JsIcons.Dice,
            large = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    item(key = "quick-row") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                title = "Snack time",
                onClick = {},
                illustration = FoodIllustration.Cookie,
                palette = CardPalettes.MexicanTile,
                icon = JsIcons.Cookie,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                title = "Under 30 min",
                onClick = {},
                illustration = FoodIllustration.Skillet,
                palette = CardPalettes.PinkBeige,
                icon = JsIcons.Bolt,
                modifier = Modifier.weight(1f),
            )
        }
    }
    item(key = "card-plain") {
        JsCard(onClick = {}) {
            Column(Modifier.padding(20.dp)) {
                Text("JsCard", style = JsTheme.typography.titleLarge)
                Text(
                    "Dove Wing surface, 28 dp corners, resting shadow. Tap it to feel the spring.",
                    style = JsTheme.typography.bodyMedium,
                )
            }
        }
    }
    item(key = "tiles") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                RecipeImageRequest("Tomato Soup", RecipeType.Meal, listOf("Soups")),
                RecipeImageRequest("Banana Bread", RecipeType.Dessert, listOf("Baking")),
                RecipeImageRequest("Iced Chai", RecipeType.Drink),
            ).forEach { request ->
                RecipeTile(
                    title = request.title,
                    image = DefaultRecipeImageProvider.imageFor(request),
                    note = request.type?.label,
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
