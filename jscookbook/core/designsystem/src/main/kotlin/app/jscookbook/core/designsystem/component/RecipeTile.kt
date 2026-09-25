package app.jscookbook.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.recipeimage.RecipeArt
import app.jscookbook.core.designsystem.recipeimage.RecipeImage
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow

/** A photo-forward recipe tile: big image, title underneath, optional note. */
@Composable
fun RecipeTile(
    title: String,
    image: RecipeImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
    aspectRatio: Float = 4f / 5f,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = JsTheme.shapes.tile
    Column(
        modifier
            .springyPress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        RecipeArt(
            image = image,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .softShadow(JsElevations.Resting, shape, JsTheme.extendedColors.shadow)
                .clip(shape),
        )
        Text(
            text = title,
            style = JsTheme.typography.titleMedium,
            color = JsTheme.colors.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp),
        )
        if (note != null) {
            Text(
                text = note,
                style = JsTheme.typography.bodySmall,
                color = JsTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}
