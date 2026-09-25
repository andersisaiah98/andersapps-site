package app.jscookbook.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme

/**
 * A pill of mutually exclusive options with a thumb that springs to the selection. The thumb
 * is drawn in the draw phase from the animation value, so sliding never recomposes.
 */
@Composable
fun JsSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JsTheme.colors
    val extended = JsTheme.extendedColors
    val reduced = JsTheme.reducedMotion
    val haptics: HapticFeedback = LocalHapticFeedback.current
    val position = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(selectedIndex, reduced) {
        if (reduced) position.snapTo(selectedIndex.toFloat())
        else position.animateTo(selectedIndex.toFloat(), JsSprings.Snappy.spec())
    }
    val thumbColor = extended.brand
    val inset = 4.dp
    Row(
        modifier
            .clip(JsTheme.shapes.pill)
            .background(colors.surfaceContainerHigh)
            .drawBehind {
                val insetPx = inset.toPx()
                val segment = (size.width - insetPx * 2) / options.size
                val height = size.height - insetPx * 2
                drawRoundRect(
                    color = thumbColor,
                    topLeft = Offset(insetPx + segment * position.value, insetPx),
                    size = Size(segment, height),
                    cornerRadius = CornerRadius(height / 2, height / 2),
                )
            }
            .padding(inset)
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }
            val textColor by animateColorAsState(
                targetValue = if (selected) extended.onBrand else colors.onSurface,
                animationSpec = JsTheme.motion.snappy(),
                label = "segmentText",
            )
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .springyPress(interactionSource, pressedScale = 0.94f)
                    .selectable(
                        selected = selected,
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.RadioButton,
                        onClick = {
                            if (!selected) haptics.performHapticFeedback(JsHaptics.Snap)
                            onSelect(index)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = JsTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                )
            }
        }
    }
}
