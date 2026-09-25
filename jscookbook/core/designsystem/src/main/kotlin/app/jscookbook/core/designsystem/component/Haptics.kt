package app.jscookbook.core.designsystem.component

import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * The app's haptic vocabulary. Every tactile moment uses one of these so the feel stays
 * consistent. The system "Touch feedback" setting still applies.
 */
object JsHaptics {
    /** Long-press picks something up. */
    val Lift: HapticFeedbackType = HapticFeedbackType.GestureThresholdActivate

    /** Something lands where it was dragged. */
    val Drop: HapticFeedbackType = HapticFeedbackType.GestureEnd

    /** A selection snaps into place: tabs, segments, grid density steps. */
    val Snap: HapticFeedbackType = HapticFeedbackType.SegmentTick

    /** Light repeated ticks, e.g. counting taps. */
    val Tick: HapticFeedbackType = HapticFeedbackType.SegmentFrequentTick

    /** A result appears: the randomizer reveal, a saved log. */
    val Reveal: HapticFeedbackType = HapticFeedbackType.Confirm

    /** A press on the raised ＋ and other primary taps. */
    val Press: HapticFeedbackType = HapticFeedbackType.ContextClick

    val all: List<Pair<String, HapticFeedbackType>> = listOf(
        "Lift" to Lift,
        "Drop" to Drop,
        "Snap" to Snap,
        "Tick" to Tick,
        "Reveal" to Reveal,
        "Press" to Press,
    )
}
