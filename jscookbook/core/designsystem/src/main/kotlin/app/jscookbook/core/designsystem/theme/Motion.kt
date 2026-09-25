package app.jscookbook.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** A named spring. The app animates with springs only; there are no linear tweens. */
@Immutable
data class JsSpring(
    val name: String,
    val dampingRatio: Float,
    val stiffness: Float,
    val usage: String,
) {
    fun <T> spec(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = dampingRatio, stiffness = stiffness, visibilityThreshold = visibilityThreshold)
}

object JsSprings {
    val Snappy = JsSpring("Snappy", dampingRatio = 0.82f, stiffness = 700f, usage = "Selection pills, toggles, small UI")
    val Bouncy = JsSpring("Bouncy", dampingRatio = 0.55f, stiffness = 420f, usage = "Press release, the ＋ button, lifts")
    val Gentle = JsSpring("Gentle", dampingRatio = 0.9f, stiffness = 260f, usage = "Sheets, screen changes, big surfaces")
    val Hero = JsSpring("Hero", dampingRatio = 0.86f, stiffness = 380f, usage = "Tile ↔ detail, photo dismiss")
    val Playful = JsSpring("Playful", dampingRatio = 0.42f, stiffness = 300f, usage = "Randomizer reveal, celebrations")

    /** The quick dip when a finger lands on something pressable. */
    val PressIn = JsSpring("Press in", dampingRatio = 1f, stiffness = 1600f, usage = "Finger down")

    val all = listOf(Snappy, Bouncy, Gentle, Hero, Playful, PressIn)
}

/**
 * The springs as animation specs, already adjusted for the system "Remove animations" setting:
 * when motion is reduced every spec snaps. Read it with [JsTheme.motion].
 */
@Immutable
class JsMotion(val reduced: Boolean) {
    fun <T> snappy(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.Snappy, visibilityThreshold)
    fun <T> bouncy(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.Bouncy, visibilityThreshold)
    fun <T> gentle(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.Gentle, visibilityThreshold)
    fun <T> hero(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.Hero, visibilityThreshold)
    fun <T> playful(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.Playful, visibilityThreshold)
    fun <T> pressIn(visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(JsSprings.PressIn, visibilityThreshold)

    fun <T> of(spring: JsSpring, visibilityThreshold: T? = null): FiniteAnimationSpec<T> = pick(spring, visibilityThreshold)

    private fun <T> pick(spring: JsSpring, visibilityThreshold: T?): FiniteAnimationSpec<T> =
        if (reduced) snap() else spring.spec(visibilityThreshold)
}

/**
 * True when the system animator duration scale is 0 (Settings › Accessibility › Remove
 * animations, or the developer option). Decorative motion should skip itself when this is set.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }
