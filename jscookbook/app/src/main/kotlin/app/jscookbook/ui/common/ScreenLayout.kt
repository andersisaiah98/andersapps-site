package app.jscookbook.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme
import kotlinx.coroutines.delay

/** Padding for a scrolling screen: clears the status bar on top and the floating bar at the bottom. */
@Composable
fun screenPadding(scaffoldPadding: PaddingValues, horizontal: Dp = 20.dp): PaddingValues {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return PaddingValues(
        start = horizontal,
        end = horizontal,
        top = top + 16.dp,
        bottom = scaffoldPadding.calculateBottomPadding() + 24.dp,
    )
}

@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        Text(title, style = JsTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Text(
                subtitle,
                style = JsTheme.typography.bodyLarge,
                color = JsTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Staggers a screen's sections in (fade + rise on a gentle spring) the first time it appears.
 * Doesn't replay when a tab is restored, for items composed later by scrolling, or when motion
 * is reduced.
 */
@Stable
class Entrance internal constructor(val play: Boolean) {
    internal var accepting: Boolean = play
}

@Composable
fun rememberEntrance(): Entrance {
    val reduced = JsTheme.reducedMotion
    var played by rememberSaveable { mutableStateOf(false) }
    val entrance = remember { Entrance(play = !played && !reduced) }
    LaunchedEffect(Unit) {
        played = true
        delay(700)
        entrance.accepting = false
    }
    return entrance
}

@Composable
fun StaggeredEntrance(
    entrance: Entrance,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animate = remember { entrance.accepting }
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    if (animate) {
        LaunchedEffect(Unit) {
            delay(index * 70L)
            progress.animateTo(1f, JsSprings.Gentle.spec())
        }
    }
    Box(
        modifier.graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * 28.dp.toPx()
        },
    ) {
        content()
    }
}
