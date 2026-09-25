package app.jscookbook.ui.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow
import app.jscookbook.core.model.Category

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = CardPalettes.fromKey(category.colorKey)
    val shape = JsTheme.shapes.tile
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier
            .fillMaxWidth()
            .springyPress(interactionSource)
            .softShadow(JsElevations.Resting, shape, JsTheme.extendedColors.shadow)
            .clip(shape)
            .background(palette.container)
            .clickable(interactionSource = interactionSource, indication = ripple(color = palette.content), role = Role.Button, onClick = onClick)
            .heightIn(min = 156.dp)
            .padding(16.dp),
    ) {
        Image(
            imageVector = FoodIllustration.fromKey(category.iconKey).vector,
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.ink),
            modifier = Modifier.size(64.dp).align(Alignment.End),
        )
        Spacer(Modifier.weight(1f))
        Text(category.name, style = JsTheme.typography.titleLarge, color = palette.content)
        Text(
            if (category.recipeCount == 1) "1 recipe" else "${category.recipeCount} recipes",
            style = JsTheme.typography.labelLarge,
            color = palette.content,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** The dashed "add a shelf" tile at the end of the category grid. */
@Composable
fun NewCategoryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = JsTheme.shapes.tile
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier
            .fillMaxWidth()
            .springyPress(interactionSource)
            .clip(shape)
            .border(1.5.dp, JsTheme.colors.outline, shape)
            .clickable(interactionSource = interactionSource, indication = ripple(), role = Role.Button, onClickLabel = "New category", onClick = onClick)
            .heightIn(min = 156.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(JsIcons.Plus, contentDescription = null, tint = JsTheme.extendedColors.brandText, modifier = Modifier.size(32.dp))
        Text("New category", style = JsTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    }
}
