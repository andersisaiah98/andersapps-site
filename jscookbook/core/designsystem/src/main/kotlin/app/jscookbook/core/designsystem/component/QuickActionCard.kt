package app.jscookbook.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow

/**
 * A big, colorful Home shortcut. The illustration has its own space and bleeds off the
 * bottom-right corner, so it never sits under the text. The card grows with the font size.
 */
@Composable
fun QuickActionCard(
    title: String,
    onClick: () -> Unit,
    illustration: FoodIllustration,
    palette: CardPalette,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    large: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = JsTheme.shapes.card
    Box(
        modifier
            .springyPress(interactionSource, pressedScale = 0.97f)
            .softShadow(JsElevations.Raised, shape, JsTheme.extendedColors.shadow)
            .clip(shape)
            .background(palette.container)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = palette.content),
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        if (large) {
            Row(Modifier.heightIn(min = 176.dp)) {
                Column(Modifier.weight(1f).padding(20.dp)) {
                    QuickActionText(title, subtitle, icon, palette, large = true)
                }
                QuickActionArt(
                    illustration = illustration,
                    palette = palette,
                    modifier = Modifier.align(Alignment.Bottom).size(148.dp).offset(x = 18.dp, y = 22.dp),
                    tilt = -10f,
                )
            }
        } else {
            Column(Modifier.heightIn(min = 150.dp).padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
                QuickActionText(title, subtitle, icon, palette, large = false)
                Spacer(Modifier.weight(1f))
                QuickActionArt(
                    illustration = illustration,
                    palette = palette,
                    modifier = Modifier.align(Alignment.End).size(92.dp).offset(x = 30.dp, y = 14.dp),
                    tilt = -14f,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.QuickActionText(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    palette: CardPalette,
    large: Boolean,
) {
    if (icon != null) {
        Box(
            Modifier
                .padding(bottom = 14.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.content.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = palette.content, modifier = Modifier.size(20.dp))
        }
    }
    Text(
        text = title,
        style = if (large) JsTheme.typography.headlineSmall else JsTheme.typography.titleLarge,
        color = palette.content,
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            style = JsTheme.typography.bodyMedium,
            color = palette.content,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun QuickActionArt(illustration: FoodIllustration, palette: CardPalette, tilt: Float, modifier: Modifier) {
    Box(modifier) {
        Image(
            imageVector = illustration.vector,
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.ink),
            alpha = 0.92f,
            modifier = Modifier.fillMaxSize().rotate(tilt),
        )
    }
}
