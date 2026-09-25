package app.jscookbook.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow
import kotlin.math.abs

private val BarMinHeight = 68.dp
private val BarPadding = 6.dp
private val AddButtonSize = 64.dp

/** How far the ＋ rises above the bar's top edge. */
private val AddButtonRise = 22.dp

/** Room left in the middle of the bar for the ＋. */
private val CenterGap = 76.dp

/** Labels grow with the user's font size up to this factor, so all four always fit on one line. */
private const val MaxLabelFontScale = 1.3f

/**
 * A floating pill bar with a spring-driven selection pill and a raised ＋ in the middle.
 * The pill's position is animated as a fractional tab index and drawn in the draw phase, so
 * switching tabs slides it without recomposing the bar; it stretches a little with speed.
 */
@Composable
fun JsBottomBar(
    current: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    addExpanded: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = JsTheme.extendedColors
    val reduced = JsTheme.reducedMotion
    val haptics = LocalHapticFeedback.current
    val destinations = TopLevelDestination.entries
    val selectedIndex = destinations.indexOf(current)

    val pill = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(selectedIndex, reduced) {
        if (reduced) pill.snapTo(selectedIndex.toFloat())
        else pill.animateTo(selectedIndex.toFloat(), JsSprings.Snappy.spec())
    }

    val density = LocalDensity.current
    val labelDensity = remember(density) { Density(density.density, density.fontScale.coerceAtMost(MaxLabelFontScale)) }
    val barShape = JsTheme.shapes.card
    val indicatorColor = extended.navIndicator

    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
    ) {
        CompositionLocalProvider(LocalDensity provides labelDensity) {
            Row(
                Modifier
                    .padding(top = AddButtonRise)
                    .fillMaxWidth()
                    .heightIn(min = BarMinHeight)
                    .softShadow(JsElevations.Raised, barShape, extended.shadow)
                    .clip(barShape)
                    .background(extended.navBar)
                    .drawBehind {
                        val pad = BarPadding.toPx()
                        val gap = CenterGap.toPx()
                        val itemWidth = (size.width - pad * 2 - gap) / destinations.size
                        val index = pill.value
                        val stretch = (abs(pill.velocity) * 0.05f).coerceAtMost(0.35f) * itemWidth
                        val left = pad + index * itemWidth + gap * (index - 1f).coerceIn(0f, 1f)
                        val insetX = 4.dp.toPx()
                        val insetY = 8.dp.toPx()
                        val width = itemWidth - insetX * 2 + stretch
                        val height = size.height - insetY * 2
                        drawRoundRect(
                            color = indicatorColor,
                            topLeft = Offset(left + insetX - stretch / 2, insetY),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(height / 2, height / 2),
                        )
                    }
                    .selectableGroup()
                    .padding(horizontal = BarPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    if (index == destinations.size / 2) Spacer(Modifier.width(CenterGap))
                    BarItem(
                        destination = destination,
                        selected = index == selectedIndex,
                        onClick = {
                            if (index != selectedIndex) {
                                haptics.performHapticFeedback(JsHaptics.Snap)
                                onNavigate(destination)
                            }
                        },
                    )
                }
            }
        }
        AddButton(
            expanded = addExpanded,
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun RowScope.BarItem(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val extended = JsTheme.extendedColors
    val reduced = JsTheme.reducedMotion
    val interactionSource = remember { MutableInteractionSource() }
    val color by animateColorAsState(
        targetValue = if (selected) extended.navSelected else extended.navUnselected,
        animationSpec = JsTheme.motion.snappy(),
        label = "navItemColor",
    )
    // A small pop when a tab becomes selected (not on first show).
    val iconScale = remember { Animatable(1f) }
    var wasSelected by remember { mutableStateOf(selected) }
    LaunchedEffect(selected) {
        if (selected && !wasSelected && !reduced) {
            iconScale.snapTo(0.78f)
            iconScale.animateTo(1f, JsSprings.Playful.spec())
        }
        wasSelected = selected
    }
    Column(
        Modifier
            .weight(1f)
            .heightIn(min = BarMinHeight)
            .springyPress(interactionSource, pressedScale = 0.9f)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .testTag("nav:${destination.testTag}")
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) destination.activeIcon else destination.icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                },
        )
        Text(
            text = destination.label,
            style = JsTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun AddButton(expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val extended = JsTheme.extendedColors
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    // ＋ turns into × while the add sheet is open.
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = JsTheme.motion.bouncy(),
        label = "addRotation",
    )
    val dome = Brush.verticalGradient(listOf(lerp(extended.brand, Color.White, 0.14f), extended.brand))
    Box(
        modifier
            .size(AddButtonSize)
            .springyPress(interactionSource, pressedScale = 0.88f)
            .softShadow(JsElevations.Floating, CircleShape, extended.shadow)
            .clip(CircleShape)
            .background(dome)
            .semantics { contentDescription = "Add" }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = extended.onBrand),
                role = Role.Button,
                onClickLabel = "Add a recipe, photo or category",
                onClick = {
                    haptics.performHapticFeedback(JsHaptics.Press)
                    onClick()
                },
            )
            .testTag("nav:add"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = JsIcons.Plus,
            contentDescription = null,
            tint = extended.onBrand,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}
