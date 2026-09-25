package app.jscookbook.ui.add

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AddChoice(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    NewRecipe("New recipe", "Type it in, step by step", JsIcons.Pencil),
    SnapPhoto("Snap a photo", "Start from the dish or a cookbook page", JsIcons.Camera),
    NewCategory("New category", "A shelf like Soups or Weeknight", JsIcons.Folder),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSheet(onDismiss: () -> Unit, onChoose: (AddChoice) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val choose: (AddChoice) -> Unit = { choice ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            onChoose(choice)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = JsTheme.shapes.sheet,
        containerColor = JsTheme.colors.surfaceContainerLow,
        contentColor = JsTheme.colors.onSurface,
        dragHandle = { SheetHandle() },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                .testTag("sheet:add"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Add to the cook book",
                style = JsTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            AddChoice.entries.forEachIndexed { index, choice ->
                AddChoiceRow(choice = choice, index = index, onClick = { choose(choice) })
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .size(width = 40.dp, height = 5.dp)
            .clip(CircleShape)
            .background(JsTheme.colors.outline.copy(alpha = 0.5f)),
    )
}

@Composable
private fun AddChoiceRow(choice: AddChoice, index: Int, onClick: () -> Unit) {
    val reduced = JsTheme.reducedMotion
    val appear = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            delay(50L * index)
            appear.animateTo(1f, JsSprings.Bouncy.spec())
        }
    }
    JsCard(
        onClick = onClick,
        color = JsTheme.colors.surfaceContainer,
        elevation = JsElevations.Resting,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value.coerceIn(0f, 1f)
                translationY = (1f - appear.value) * 32.dp.toPx()
            },
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JsTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(choice.icon, contentDescription = null, tint = JsTheme.colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(choice.title, style = JsTheme.typography.titleMedium)
                Text(choice.subtitle, style = JsTheme.typography.bodyMedium, color = JsTheme.colors.onSurfaceVariant)
            }
            Icon(JsIcons.ChevronRight, contentDescription = null, tint = JsTheme.colors.onSurfaceVariant)
        }
    }
}
