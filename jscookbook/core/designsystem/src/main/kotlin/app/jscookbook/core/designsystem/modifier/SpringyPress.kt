package app.jscookbook.core.designsystem.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Constraints
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.LocalReducedMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Springs the content down to [pressedScale] while a finger is on it and bounces it back on
 * release. A quick tap still gets the full dip and bounce. The scale is applied in a graphics
 * layer, so presses never recompose anything. Does nothing when motion is reduced.
 *
 * Share [interactionSource] with the clickable this decorates.
 */
fun Modifier.springyPress(interactionSource: InteractionSource, pressedScale: Float = 0.96f): Modifier =
    this then SpringyPressElement(interactionSource, pressedScale)

private data class SpringyPressElement(
    val interactionSource: InteractionSource,
    val pressedScale: Float,
) : ModifierNodeElement<SpringyPressNode>() {
    override fun create() = SpringyPressNode(interactionSource, pressedScale)

    override fun update(node: SpringyPressNode) {
        node.update(interactionSource, pressedScale)
    }
}

private class SpringyPressNode(
    private var interactionSource: InteractionSource,
    private var pressedScale: Float,
) : Modifier.Node(), LayoutModifierNode, CompositionLocalConsumerModifierNode {

    private val scale = Animatable(1f)
    private var collectJob: Job? = null

    override fun onAttach() {
        collectInteractions()
    }

    fun update(interactionSource: InteractionSource, pressedScale: Float) {
        this.pressedScale = pressedScale
        if (interactionSource != this.interactionSource) {
            this.interactionSource = interactionSource
            collectJob?.cancel()
            collectInteractions()
        }
    }

    private fun collectInteractions() {
        collectJob = coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                if (currentValueOf(LocalReducedMotion)) return@collect
                when (interaction) {
                    is PressInteraction.Press -> launch {
                        scale.animateTo(pressedScale, JsSprings.PressIn.spec())
                    }
                    is PressInteraction.Release -> launch {
                        if (scale.value > pressedScale + 0.005f) {
                            scale.animateTo(pressedScale, JsSprings.PressIn.spec())
                        }
                        scale.animateTo(1f, JsSprings.Bouncy.spec())
                    }
                    is PressInteraction.Cancel -> launch {
                        scale.animateTo(1f, JsSprings.Bouncy.spec())
                    }
                }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                val s = scale.value
                scaleX = s
                scaleY = s
            }
        }
    }
}
