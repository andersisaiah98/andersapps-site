package app.jscookbook.ui.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Category
import app.jscookbook.ui.common.LocalMessenger
import kotlinx.coroutines.launch

/** New or edit category: name, an icon from the illustration set and a palette color. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorSheet(
    category: Category?,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: CategoryActionsViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val messenger = LocalMessenger.current
    val haptics = LocalHapticFeedback.current
    var name by rememberSaveable { mutableStateOf(category?.name.orEmpty()) }
    var iconKey by rememberSaveable { mutableStateOf(category?.iconKey ?: FoodIllustration.Bowl.name) }
    var colorKey by rememberSaveable { mutableStateOf(category?.colorKey ?: CardPalettes.TerraCotta.key) }
    val close: (after: () -> Unit) -> Unit = { after ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            after()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = JsTheme.shapes.sheet,
        containerColor = JsTheme.colors.surfaceContainerLow,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(if (category == null) "New category" else "Edit category", style = JsTheme.typography.headlineSmall)

            CategoryCard(
                category = Category(
                    id = category?.id ?: "preview",
                    name = name.ifBlank { "Your category" },
                    iconKey = iconKey,
                    colorKey = colorKey,
                    position = 0,
                    recipeCount = category?.recipeCount ?: 0,
                ),
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.55f).align(Alignment.CenterHorizontally),
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                shape = JsTheme.shapes.control,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Icon", style = JsTheme.typography.titleMedium)
            FlowRow(
                Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FoodIllustration.entries.forEach { illustration ->
                    val selected = illustration.name == iconKey
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (selected) JsTheme.colors.primaryContainer else JsTheme.colors.surfaceContainer)
                            .selectable(selected = selected, role = Role.RadioButton, onClick = {
                                haptics.performHapticFeedback(JsHaptics.Snap)
                                iconKey = illustration.name
                            })
                            .semantics { contentDescription = illustration.label },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            illustration.vector,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (selected) JsTheme.colors.onPrimaryContainer else JsTheme.colors.onSurfaceVariant,
                            ),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            Text("Color", style = JsTheme.typography.titleMedium)
            FlowRow(
                Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CardPalettes.all.forEach { palette ->
                    val selected = palette.key == colorKey
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.container)
                            .then(if (selected) Modifier.border(3.dp, JsTheme.colors.onBackground, CircleShape) else Modifier)
                            .selectable(selected = selected, role = Role.RadioButton, onClick = {
                                haptics.performHapticFeedback(JsHaptics.Snap)
                                colorKey = palette.key
                            })
                            .semantics { contentDescription = palette.name },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (category != null) {
                    JsButton(
                        "Delete",
                        style = JsButtonStyle.Text,
                        icon = JsIcons.Trash,
                        onClick = {
                            viewModel.delete(category.id)
                            close {
                                onDeleted()
                                messenger.show("Deleted “${category.name}”", actionLabel = "Undo") {
                                    viewModel.restore(category.id)
                                }
                            }
                        },
                    )
                }
                Box(Modifier.weight(1f))
                JsButton(
                    if (category == null) "Create" else "Save",
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.save(category?.id, name, iconKey, colorKey)
                        haptics.performHapticFeedback(JsHaptics.Reveal)
                        close {}
                    },
                )
            }
        }
    }
}
