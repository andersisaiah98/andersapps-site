package app.jscookbook.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A soft, layered shadow: a tight contact shadow plus a wide diffuse one. Both are hardware
 * RenderNode shadows, so they cost almost nothing to draw or animate.
 */
@Immutable
data class JsElevation(val name: String, val contact: Dp, val diffuse: Dp)

object JsElevations {
    val Flat = JsElevation("Flat", 0.dp, 0.dp)
    val Resting = JsElevation("Resting", 1.dp, 6.dp)
    val Raised = JsElevation("Raised", 2.dp, 12.dp)
    val Floating = JsElevation("Floating", 3.dp, 18.dp)

    /** A tile picked up by a drag. */
    val Lifted = JsElevation("Lifted", 6.dp, 28.dp)

    val all = listOf(Flat, Resting, Raised, Floating, Lifted)
}

/** Draws [elevation] as two warm-tinted shadows. Pass [JsExtendedColors.shadow] as [color]. */
fun Modifier.softShadow(elevation: JsElevation, shape: Shape, color: Color): Modifier {
    if (elevation.diffuse == 0.dp && elevation.contact == 0.dp) return this
    return this
        .shadow(
            elevation = elevation.diffuse,
            shape = shape,
            clip = false,
            ambientColor = color.copy(alpha = 0.6f),
            spotColor = color.copy(alpha = 0.7f),
        )
        .shadow(
            elevation = elevation.contact,
            shape = shape,
            clip = false,
            ambientColor = color,
            spotColor = color,
        )
}
