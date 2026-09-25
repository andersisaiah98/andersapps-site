package app.jscookbook.core.designsystem.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.theme.JsSpring
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme
import kotlinx.coroutines.launch

internal fun LazyListScope.motionSection() {
    catalogHeader(
        "motion",
        "Motion",
        "Named springs. Tap a track to fling the dot; it squashes with the spring's velocity.",
    )
    item(key = "motion-reduced") {
        val reduced = JsTheme.reducedMotion
        Text(
            if (reduced) "Remove animations is ON: every spring snaps, decorative motion is skipped."
            else "Remove animations is off: springs run normally.",
            style = JsTheme.typography.bodyMedium,
            color = JsTheme.extendedColors.brandText,
        )
    }
    items(JsSprings.all, key = { "spring:" + it.name }) { spring -> SpringDemo(spring) }
}

@Composable
private fun SpringDemo(spring: JsSpring) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val motion = JsTheme.motion
    val track = JsTheme.colors.surfaceContainerHighest
    val dot = JsTheme.extendedColors.brand
    val fling = {
        scope.launch { progress.animateTo(if (progress.targetValue < 0.5f) 1f else 0f, motion.of(spring)) }
        Unit
    }
    JsCard {
        Column(Modifier.padding(16.dp)) {
            Text(spring.name, style = JsTheme.typography.titleMedium)
            Text(
                "damping ${spring.dampingRatio} · stiffness ${spring.stiffness.toInt()} — ${spring.usage}",
                style = JsTheme.typography.bodySmall,
                color = JsTheme.colors.onSurfaceVariant,
            )
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(JsTheme.shapes.pill)
                    .clickable(role = Role.Button, onClickLabel = "Play ${spring.name}") { fling() }
                    .drawBehind {
                        val r = size.height / 2
                        drawRoundRect(track, cornerRadius = CornerRadius(r, r))
                        val dotRadius = r * 0.7f
                        val travel = size.width - r * 2
                        // Squash along the direction of travel, from the spring's own velocity.
                        val stretch = (1f + kotlin.math.abs(progress.velocity) * 0.04f).coerceAtMost(1.6f)
                        val center = Offset(r + travel * progress.value, r)
                        drawRoundRect(
                            color = dot,
                            topLeft = Offset(center.x - dotRadius * stretch, center.y - dotRadius / stretch),
                            size = Size(dotRadius * 2 * stretch, dotRadius * 2 / stretch),
                            cornerRadius = CornerRadius(dotRadius, dotRadius),
                        )
                    },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.hapticsSection() {
    catalogHeader("haptics", "Haptics", "The whole vocabulary. Try each one; the system Touch feedback setting still applies.")
    item(key = "haptics") {
        val haptics = LocalHapticFeedback.current
        JsCard {
            FlowRow(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JsHaptics.all.forEach { (name, type) ->
                    JsButton(name, onClick = { haptics.performHapticFeedback(type) }, style = JsButtonStyle.Tonal)
                }
            }
        }
    }
}
