package app.jscookbook.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme

/**
 * 1–5 stars. Pass [onRate] to make it editable: each star is a 48 dp target, tapping the current
 * rating clears it, and the newly chosen star pops.
 */
@Composable
fun StarRating(
    rating: Int?,
    modifier: Modifier = Modifier,
    onRate: ((Int?) -> Unit)? = null,
    starSize: Dp = 22.dp,
) {
    val haptics = LocalHapticFeedback.current
    val reduced = JsTheme.reducedMotion
    val filledColor = JsTheme.extendedColors.brandText
    val emptyColor = JsTheme.colors.outline
    val value = rating ?: 0
    val groupModifier = if (onRate != null) {
        Modifier.selectableGroup()
    } else {
        Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (rating == null) "Not rated" else "Rated $rating out of 5"
        }
    }
    Row(modifier.then(groupModifier), verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { star ->
            val filled = star <= value
            val pop = remember { Animatable(1f) }
            LaunchedEffect(value) {
                if (onRate != null && !reduced && star == value) {
                    pop.snapTo(0.6f)
                    pop.animateTo(1f, JsSprings.Playful.spec())
                }
            }
            val tint = if (filled) filledColor else emptyColor
            val icon = if (filled) JsIcons.StarFilled else JsIcons.Star
            if (onRate != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(48.dp)
                        .selectable(
                            selected = star == value,
                            role = Role.RadioButton,
                            onClick = {
                                haptics.performHapticFeedback(JsHaptics.Snap)
                                onRate(if (star == value) null else star)
                            },
                        )
                        .semantics { contentDescription = if (star == 1) "1 star" else "$star stars" }
                        .graphicsLayer {
                            scaleX = pop.value * 0.6f
                            scaleY = pop.value * 0.6f
                        },
                )
            } else {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(starSize))
            }
        }
    }
}
