package app.jscookbook.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner radii. 20, 24 and 28 dp are the core of the look. */
object JsRadius {
    val Small = 14.dp
    val Control = 20.dp
    val Tile = 24.dp
    val Card = 28.dp
}

/** Named shapes. Plain rounded rects on purpose: they clip analytically, which keeps grids at 120 Hz. */
@Immutable
data class JsShapes(
    /** Inputs, chips, small controls. */
    val control: Shape = RoundedCornerShape(JsRadius.Control),
    /** Recipe photos and fallback art. */
    val tile: Shape = RoundedCornerShape(JsRadius.Tile),
    /** Cards, the floating nav bar, big surfaces. */
    val card: Shape = RoundedCornerShape(JsRadius.Card),
    /** Bottom sheets. */
    val sheet: Shape = RoundedCornerShape(topStart = JsRadius.Card, topEnd = JsRadius.Card),
    /** Buttons and pills. */
    val pill: Shape = CircleShape,
)

internal val JsMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(JsRadius.Small),
    medium = RoundedCornerShape(JsRadius.Control),
    large = RoundedCornerShape(JsRadius.Tile),
    extraLarge = RoundedCornerShape(JsRadius.Card),
)
