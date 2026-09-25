package app.jscookbook.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.theme.JsTheme

enum class JsButtonStyle { Primary, Tonal, Outline, Text }

/** A pill button that dips and bounces under the finger. */
@Composable
fun JsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: JsButtonStyle = JsButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = JsTheme.colors
    val extended = JsTheme.extendedColors
    val (container, content) = when (style) {
        JsButtonStyle.Primary -> extended.brand to extended.onBrand
        JsButtonStyle.Tonal -> colors.primaryContainer to colors.onPrimaryContainer
        JsButtonStyle.Outline, JsButtonStyle.Text -> Color.Transparent to extended.brandText
    }
    Button(
        onClick = onClick,
        modifier = modifier.springyPress(interactionSource),
        enabled = enabled,
        shape = JsTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (container == Color.Transparent) Color.Transparent else colors.onSurface.copy(alpha = 0.10f),
            disabledContentColor = colors.onSurface.copy(alpha = 0.45f),
        ),
        elevation = null,
        border = if (style == JsButtonStyle.Outline) {
            BorderStroke(1.5.dp, if (enabled) colors.outline else colors.outline.copy(alpha = 0.4f))
        } else {
            null
        },
        contentPadding = if (style == JsButtonStyle.Text) {
            PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        } else {
            PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        },
        interactionSource = interactionSource,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = JsTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

/** A 48 dp icon button with the same springy press. [contentDescription] is required for TalkBack. */
@Composable
fun JsIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = JsTheme.colors.onSurface,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = modifier.springyPress(interactionSource, pressedScale = 0.88f),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
        interactionSource = interactionSource,
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}
