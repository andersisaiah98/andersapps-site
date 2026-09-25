package app.jscookbook.ui.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.component.StarRating
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.recipeimage.LocalRecipeImageProvider
import app.jscookbook.core.designsystem.recipeimage.RecipeArt
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.IngredientParser
import app.jscookbook.core.model.Recipe
import app.jscookbook.core.model.formatCountdown
import app.jscookbook.core.model.formatMinutes
import app.jscookbook.ui.common.LocalMessenger
import app.jscookbook.ui.common.recipeImageSharedElement
import app.jscookbook.ui.common.thumbnailImageRequest
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun RecipeDetailRoute(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenPhoto: (recipeId: String, index: Int) -> Unit,
    onOpenCategory: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val checked by viewModel.checked.collectAsStateWithLifecycle()
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(viewModel) {
        viewModel.timerDone.collect { step ->
            haptics.performHapticFeedback(JsHaptics.Reveal)
            messenger.show("Step $step timer is done")
        }
    }
    LaunchedEffect(uiState) {
        if (uiState is RecipeDetailUiState.Missing) onBack()
    }

    val recipe = (uiState as? RecipeDetailUiState.Ready)?.recipe ?: return
    RecipeDetailScreen(
        recipe = recipe,
        origin = viewModel.origin,
        checked = checked,
        timers = timers,
        onBack = onBack,
        onEdit = { onEdit(recipe.id) },
        onToggleFavorite = {
            haptics.performHapticFeedback(if (recipe.isFavorite) JsHaptics.Tick else JsHaptics.Reveal)
            viewModel.toggleFavorite()
        },
        onToggleIngredient = viewModel::toggleIngredient,
        onToggleTimer = viewModel::toggleTimer,
        onResetTimer = viewModel::resetTimer,
        onOpenPhoto = { onOpenPhoto(recipe.id, it) },
        onOpenCategory = onOpenCategory,
        onDelete = {
            viewModel.delete()
            onBack()
            messenger.show("Deleted “${recipe.title}”", actionLabel = "Undo") { viewModel.restore() }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    origin: String,
    checked: Set<String>,
    timers: Map<String, StepTimer>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleIngredient: (String) -> Unit,
    onToggleTimer: (stepId: String, stepNumber: Int, seconds: Int) -> Unit,
    onResetTimer: (String) -> Unit,
    onOpenPhoto: (Int) -> Unit,
    onOpenCategory: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val dismiss = rememberPullToDismiss(onDismiss = onBack)
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(modifier.fillMaxSize().testTag("screen:recipe")) {
        val heroHeight = min(maxWidth * 1.05f, maxHeight * 0.58f)
        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(dismiss.connection)
                .graphicsLayer {
                    val progress = dismiss.progress
                    val scale = 1f - 0.14f * progress
                    scaleX = scale
                    scaleY = scale
                    translationY = dismiss.offset.value * 0.35f
                    shape = RoundedCornerShape((32 * progress).dp)
                    clip = progress > 0f
                }
                .background(JsTheme.colors.background),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = bottom + 40.dp),
            ) {
                item(key = "hero") {
                    Hero(recipe, origin, Modifier.fillMaxWidth().height(heroHeight))
                }
                item(key = "title") { TitleBlock(recipe, onOpenCategory) }
                if (recipe.description.isNotBlank()) {
                    item(key = "description") {
                        Text(
                            recipe.description,
                            style = JsTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }
                }
                if (recipe.ingredients.isNotEmpty()) {
                    item(key = "ingredients-title") { SectionTitle("Ingredients") }
                    var lastSection: String? = null
                    recipe.ingredients.forEach { ingredient ->
                        if (ingredient.section != null && ingredient.section != lastSection) {
                            val section = ingredient.section!!
                            item(key = "section:${ingredient.id}") {
                                Text(
                                    section,
                                    style = JsTheme.typography.titleSmall,
                                    color = JsTheme.extendedColors.brandText,
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 2.dp),
                                )
                            }
                        }
                        lastSection = ingredient.section
                        item(key = "ingredient:${ingredient.id}") {
                            IngredientRow(
                                text = IngredientParser.format(ingredient.quantity, ingredient.unit, ingredient.name, ingredient.note),
                                checked = ingredient.id in checked,
                                onToggle = { onToggleIngredient(ingredient.id) },
                            )
                        }
                    }
                }
                if (recipe.steps.isNotEmpty()) {
                    item(key = "steps-title") { SectionTitle("Steps") }
                    itemsIndexed(recipe.steps, key = { _, step -> "step:${step.id}" }) { index, step ->
                        StepRow(
                            number = index + 1,
                            text = step.text,
                            timerSeconds = step.timerSeconds,
                            timer = timers[step.id],
                            onToggleTimer = { step.timerSeconds?.let { onToggleTimer(step.id, index + 1, it) } },
                            onResetTimer = { onResetTimer(step.id) },
                        )
                    }
                }
                if (recipe.photos.isNotEmpty()) {
                    item(key = "photos") { PhotoStrip(recipe, onOpenPhoto) }
                }
                if (recipe.notes.isNotBlank() || recipe.tags.isNotEmpty() || recipe.sourceUrl.isNotBlank()) {
                    item(key = "notes") { NotesBlock(recipe) }
                }
                item(key = "delete") {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        JsButton("Delete recipe", onClick = { confirmDelete = true }, style = JsButtonStyle.Text, icon = JsIcons.Trash)
                    }
                }
            }
            TopButtons(
                isFavorite = recipe.isFavorite,
                onBack = onBack,
                onEdit = onEdit,
                onToggleFavorite = onToggleFavorite,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete “${recipe.title}”?") },
            text = { Text("You can undo this right after.") },
            confirmButton = {
                JsButton("Delete", onClick = {
                    confirmDelete = false
                    onDelete()
                })
            },
            dismissButton = { JsButton("Keep", onClick = { confirmDelete = false }, style = JsButtonStyle.Text) },
            shape = JsTheme.shapes.card,
            containerColor = JsTheme.colors.surfaceContainerHigh,
        )
    }
}

@Composable
private fun Hero(recipe: Recipe, origin: String, modifier: Modifier) {
    val provider = LocalRecipeImageProvider.current
    val thumbnail = remember(recipe.id, recipe.cover?.thumbnailPath, recipe.title, recipe.type, recipe.categories) {
        provider.imageFor(recipe.thumbnailImageRequest())
    }
    val fullPath = recipe.cover?.localPath
    Box(modifier.recipeImageSharedElement(recipe.id, origin)) {
        // The thumbnail is what the tile showed, so the flight in is seamless; the full-size image
        // lands on top once decoded.
        RecipeArt(thumbnail, Modifier.fillMaxSize(), contentDescription = null)
        if (fullPath != null) {
            AsyncImage(
                model = File(fullPath),
                contentDescription = "Photo of ${recipe.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent))),
        )
    }
}

@Composable
private fun TopButtons(isFavorite: Boolean, onBack: () -> Unit, onEdit: () -> Unit, onToggleFavorite: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassButton(JsIcons.Back, "Back", onBack)
        Spacer(Modifier.weight(1f))
        GlassButton(JsIcons.Heart, if (isFavorite) "Remove from favorites" else "Add to favorites", onToggleFavorite, active = isFavorite)
        GlassButton(JsIcons.Pencil, "Edit recipe", onEdit)
    }
}

@Composable
private fun GlassButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, active: Boolean = false) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        Modifier
            .size(48.dp)
            .springyPress(interactionSource, pressedScale = 0.88f)
            .clip(CircleShape)
            .background(if (active) JsTheme.extendedColors.brand else Color.Black.copy(alpha = 0.32f))
            .clickable(interactionSource = interactionSource, indication = androidx.compose.material3.ripple(color = Color.White), role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleBlock(recipe: Recipe, onOpenCategory: (String) -> Unit) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(recipe.title, style = JsTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
        val meta = listOfNotNull(
            recipe.type?.label,
            recipe.totalMinutes?.let(::formatMinutes),
            recipe.servings.takeIf { it.isNotBlank() }?.let { "Serves $it" },
        )
        if (meta.isNotEmpty()) {
            Text(meta.joinToString("  ·  "), style = JsTheme.typography.labelLarge, color = JsTheme.colors.onSurfaceVariant)
        }
        if (recipe.prepMinutes != null && recipe.cookMinutes != null) {
            Text(
                "Prep ${formatMinutes(recipe.prepMinutes!!)} · Cook ${formatMinutes(recipe.cookMinutes!!)}",
                style = JsTheme.typography.bodySmall,
                color = JsTheme.colors.onSurfaceVariant,
            )
        }
        if (recipe.rating != null) StarRating(recipe.rating)
        if (recipe.categories.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.categories.forEach { category ->
                    Text(
                        category.name,
                        style = JsTheme.typography.labelLarge,
                        color = JsTheme.colors.onPrimaryContainer,
                        modifier = Modifier
                            .clip(JsTheme.shapes.pill)
                            .background(JsTheme.colors.primaryContainer)
                            .clickable(role = Role.Button, onClickLabel = "Open ${category.name}") { onOpenCategory(category.id) }
                            .heightIn(min = 36.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = JsTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 6.dp).semantics { heading() },
    )
}

@Composable
private fun IngredientRow(text: String, checked: Boolean, onToggle: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox, onClickLabel = if (checked) "Uncheck" else "Check") {
                haptics.performHapticFeedback(JsHaptics.Tick)
                onToggle()
            }
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = JsTheme.extendedColors.brand, checkmarkColor = JsTheme.extendedColors.onBrand),
        )
        Text(
            text,
            style = JsTheme.typography.bodyLarge,
            color = if (checked) JsTheme.colors.onSurfaceVariant.copy(alpha = 0.7f) else JsTheme.colors.onBackground,
            textDecoration = if (checked) TextDecoration.LineThrough else null,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun StepRow(
    number: Int,
    text: String,
    timerSeconds: Int?,
    timer: StepTimer?,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(JsTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", style = JsTheme.typography.labelLarge, color = JsTheme.colors.onPrimaryContainer)
        }
        Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text, style = JsTheme.typography.bodyLarge)
            if (timerSeconds != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val running = timer?.running == true
                    val done = timer != null && timer.remainingSeconds == 0
                    JsButton(
                        text = when {
                            timer == null -> "Timer ${formatCountdown(timerSeconds)}"
                            done -> "Done · restart"
                            running -> "Pause ${formatCountdown(timer.remainingSeconds)}"
                            else -> "Resume ${formatCountdown(timer.remainingSeconds)}"
                        },
                        onClick = onToggleTimer,
                        icon = JsIcons.Timer,
                        style = if (running) JsButtonStyle.Primary else JsButtonStyle.Tonal,
                    )
                    if (timer != null) {
                        JsButton("Reset", onClick = onResetTimer, style = JsButtonStyle.Text)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoStrip(recipe: Recipe, onOpenPhoto: (Int) -> Unit) {
    Column {
        SectionTitle("Photos")
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(recipe.photos, key = { _, photo -> photo.id }) { index, photo ->
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                AsyncImage(
                    model = (photo.thumbnailPath ?: photo.localPath)?.let(::File),
                    contentDescription = photo.caption.ifBlank { "Photo ${index + 1}" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 120.dp, height = 150.dp)
                        .springyPress(interactionSource)
                        .clip(JsTheme.shapes.tile)
                        .background(JsTheme.colors.surfaceVariant)
                        .clickable(interactionSource = interactionSource, indication = null, role = Role.Button) { onOpenPhoto(index) },
                )
            }
        }
    }
}

@Composable
private fun NotesBlock(recipe: Recipe) {
    val uriHandler = LocalUriHandler.current
    JsCard(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (recipe.notes.isNotBlank()) {
                Text("Notes", style = JsTheme.typography.titleLarge)
                Text(recipe.notes, style = JsTheme.typography.bodyLarge)
            }
            if (recipe.tags.isNotEmpty()) {
                Text(recipe.tags.joinToString("  ") { "#$it" }, style = JsTheme.typography.labelLarge, color = JsTheme.colors.onSurfaceVariant)
            }
            if (recipe.sourceUrl.isNotBlank()) {
                Row(
                    Modifier
                        .clip(JsTheme.shapes.pill)
                        .clickable(role = Role.Button, onClickLabel = "Open the original recipe") {
                            runCatching { uriHandler.openUri(normalizedUrl(recipe.sourceUrl)) }
                        }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(JsIcons.Link, contentDescription = null, tint = JsTheme.extendedColors.brandText)
                    Text(
                        recipe.sourceUrl.removePrefix("https://").removePrefix("http://").removePrefix("www."),
                        style = JsTheme.typography.bodyMedium,
                        color = JsTheme.extendedColors.brandText,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp).width(260.dp),
                    )
                }
            }
        }
    }
}

private fun normalizedUrl(url: String) = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"

/**
 * Pull down past the top of the page to shrink it and fly the hero back into its tile. The
 * list scrolls normally; only overscroll at the top is taken, so it never fights the content.
 */
private class PullToDismissState(
    private val thresholdPx: Float,
    private val onDismiss: () -> Unit,
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val reduced: Boolean,
) {
    val offset = Animatable(0f)
    val progress: Float get() = (offset.value / (thresholdPx * 2f)).coerceIn(0f, 1f)
    private var dismissed = false

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Pushing back up while pulled: undo the pull before the list scrolls.
            if (offset.value > 0f && available.y < 0f) {
                val consumed = maxOf(available.y, -offset.value)
                scope.launch { offset.snapTo(offset.value + consumed) }
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (reduced || dismissed || source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
            // Resistance grows as it's pulled, like a rubber band.
            val resistance = 1f - (offset.value / (thresholdPx * 3f)).coerceIn(0f, 0.7f)
            scope.launch { offset.snapTo(offset.value + available.y * resistance) }
            return Offset(0f, available.y)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (offset.value <= 0f) return Velocity.Zero
            if (offset.value > thresholdPx || (available.y > 1800f && offset.value > thresholdPx / 3)) {
                dismissed = true
                onDismiss()
            } else {
                offset.animateTo(0f, JsSprings.Bouncy.spec())
            }
            return available
        }
    }
}

@Composable
private fun rememberPullToDismiss(onDismiss: () -> Unit): PullToDismissState {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val reduced = JsTheme.reducedMotion
    val haptics = LocalHapticFeedback.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val state = remember(density, reduced) {
        PullToDismissState(
            thresholdPx = with(density) { 120.dp.toPx() },
            onDismiss = {
                haptics.performHapticFeedback(JsHaptics.Drop)
                currentOnDismiss()
            },
            scope = scope,
            reduced = reduced,
        )
    }
    return state
}
