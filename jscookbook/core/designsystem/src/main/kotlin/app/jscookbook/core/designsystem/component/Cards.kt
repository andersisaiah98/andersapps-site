package app.jscookbook.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.theme.JsElevation
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow

/** A card surface: 28 dp corners, layered warm shadow, springy press when clickable. */
@Composable
fun JsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    color: Color = JsTheme.colors.surfaceContainer,
    contentColor: Color = JsTheme.colors.onSurface,
    shape: Shape = JsTheme.shapes.card,
    elevation: JsElevation = JsElevations.Resting,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shadow = JsTheme.extendedColors.shadow
    Column(
        modifier
            .then(if (onClick != null) Modifier.springyPress(interactionSource, pressedScale = 0.97f) else Modifier)
            .softShadow(elevation, shape, shadow)
            .clip(shape)
            .background(color)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = contentColor),
                        onClickLabel = onClickLabel,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
