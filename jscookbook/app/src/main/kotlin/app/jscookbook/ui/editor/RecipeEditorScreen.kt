package app.jscookbook.ui.editor

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.component.JsIconButton
import app.jscookbook.core.designsystem.component.StarRating
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.RecipeType
import app.jscookbook.ui.common.LocalMessenger
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun RecipeEditorRoute(
    onClose: () -> Unit,
    onSaved: (id: String, isNew: Boolean) -> Unit,
    onDeleted: () -> Unit,
    onNewCategory: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val pickPhotos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)) { uris ->
        viewModel.addPhotos(uris)
    }
    // The camera writes into a file we own; its path survives the trip to the camera app.
    var captureFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val path = captureFilePath ?: return@rememberLauncherForActivityResult
        val file = File(path)
        if (taken) viewModel.addPhotos(listOf(Uri.fromFile(file))) { file.delete() } else file.delete()
        captureFilePath = null
    }
    val openCamera: () -> Unit = {
        val file = viewModel.newCaptureFile()
        captureFilePath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        runCatching { takePicture.launch(uri) }.onFailure { messenger.show("No camera app available") }
    }

    LaunchedEffect(state.loading) {
        if (!state.loading && viewModel.cameraPending) {
            viewModel.cameraHandled()
            openCamera()
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.Saved -> {
                    haptics.performHapticFeedback(JsHaptics.Reveal)
                    onSaved(event.id, event.isNew)
                }
                EditorEvent.Deleted -> {
                    val id = state.id
                    onDeleted()
                    messenger.show("Deleted “${state.title}”", actionLabel = "Undo") { viewModel.restore(id) }
                }
                is EditorEvent.Message -> messenger.show(event.text)
            }
        }
    }

    val close: () -> Unit = {
        if (state.dirty) confirmDiscard = true else onClose()
    }
    BackHandler(enabled = state.dirty) { confirmDiscard = true }

    RecipeEditorScreen(
        state = state,
        categories = categories,
        viewModel = viewModel,
        onClose = close,
        onAddPhotos = { pickPhotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onCamera = openCamera,
        onNewCategory = onNewCategory,
        onDelete = { confirmDelete = true },
    )

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(if (state.isNew) "Discard this recipe?" else "Discard your changes?") },
            confirmButton = {
                JsButton("Discard", onClick = {
                    confirmDiscard = false
                    viewModel.discard()
                    onClose()
                })
            },
            dismissButton = { JsButton("Keep editing", onClick = { confirmDiscard = false }, style = JsButtonStyle.Text) },
            shape = JsTheme.shapes.card,
            containerColor = JsTheme.colors.surfaceContainerHigh,
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete “${state.title}”?") },
            text = { Text("You can undo this right after.") },
            confirmButton = {
                JsButton("Delete", onClick = {
                    confirmDelete = false
                    viewModel.delete()
                })
            },
            dismissButton = { JsButton("Keep", onClick = { confirmDelete = false }, style = JsButtonStyle.Text) },
            shape = JsTheme.shapes.card,
            containerColor = JsTheme.colors.surfaceContainerHigh,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeEditorScreen(
    state: EditorUiState,
    categories: List<Category>,
    viewModel: RecipeEditorViewModel,
    onClose: () -> Unit,
    onAddPhotos: () -> Unit,
    onCamera: () -> Unit,
    onNewCategory: () -> Unit,
    onDelete: () -> Unit,
) {
    var pasteOpen by rememberSaveable { mutableStateOf(false) }
    var photoMenu by remember { mutableStateOf<Photo?>(null) }

    Column(Modifier.fillMaxSize().testTag("screen:editor").imePadding()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JsIconButton(JsIcons.Close, contentDescription = "Close", onClick = onClose)
            Text(
                if (state.isNew) "New recipe" else "Edit recipe",
                style = JsTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() },
            )
            JsButton("Save", onClick = viewModel::save, enabled = !state.saving && !state.loading, modifier = Modifier.padding(end = 8.dp))
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "photos") {
                PhotoRow(state, onAddPhotos = onAddPhotos, onCamera = onCamera, onPhotoClick = { photoMenu = it })
            }
            item(key = "title") {
                Field(
                    value = state.title,
                    onValueChange = viewModel::onTitle,
                    label = "Recipe name",
                    textStyle = JsTheme.typography.headlineSmall,
                    isError = state.showTitleError,
                    capitalization = KeyboardCapitalization.Words,
                    modifier = Modifier.testTag("editor:title"),
                )
            }
            item(key = "type") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Label("Type")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RecipeType.entries.forEach { type ->
                            Chip(type.label, selected = state.type == type) { viewModel.onType(if (state.type == type) null else type) }
                        }
                    }
                }
            }
            item(key = "categories") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Label("Categories")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { category ->
                            Chip(category.name, selected = category.id in state.categoryIds) { viewModel.toggleCategory(category.id) }
                        }
                        Chip("New category", selected = false, icon = JsIcons.Plus, onClick = onNewCategory)
                    }
                }
            }
            item(key = "times") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field(state.prepMinutes, viewModel::onPrep, "Prep min", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    Field(state.cookMinutes, viewModel::onCook, "Cook min", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    Field(state.servings, viewModel::onServings, "Serves", Modifier.weight(1f))
                }
            }
            item(key = "favorite") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Label("Rating")
                        StarRating(state.rating, onRate = viewModel::onRating)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Label("Favorite")
                        Switch(
                            checked = state.isFavorite,
                            onCheckedChange = viewModel::onFavorite,
                            colors = SwitchDefaults.colors(checkedTrackColor = JsTheme.extendedColors.brand),
                        )
                    }
                }
            }
            item(key = "description") {
                Field(state.description, viewModel::onDescription, "Short description", singleLine = false, minLines = 2)
            }

            item(key = "ingredients-header") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                    SectionLabel("Ingredients", Modifier.weight(1f))
                    JsButton("Paste a list", onClick = { pasteOpen = true }, style = JsButtonStyle.Text)
                }
            }
            item(key = "ingredients-hint") {
                Text(
                    "One per line, like “2 cups flour, sifted”. End a line with “:” for a heading.",
                    style = JsTheme.typography.bodySmall,
                    color = JsTheme.colors.onSurfaceVariant,
                )
            }
            itemsIndexed(state.ingredients, key = { _, line -> "ingredient:${line.id}" }) { index, line ->
                ReorderableRow(
                    canMoveUp = index > 0,
                    canMoveDown = index < state.ingredients.lastIndex,
                    onMove = { viewModel.moveIngredient(line.id, it) },
                    onRemove = { viewModel.removeIngredient(line.id) },
                    itemLabel = "ingredient ${index + 1}",
                ) {
                    Field(
                        value = line.text,
                        onValueChange = { viewModel.onIngredientText(line.id, it) },
                        label = if (line.text.trimEnd().endsWith(":")) "Heading" else "Ingredient",
                        imeAction = ImeAction.Next,
                        onImeAction = { if (index == state.ingredients.lastIndex) viewModel.addIngredient(line.id) },
                    )
                }
            }
            item(key = "add-ingredient") {
                JsButton("Add ingredient", onClick = { viewModel.addIngredient() }, icon = JsIcons.Plus, style = JsButtonStyle.Tonal)
            }

            item(key = "steps-header") { SectionLabel("Steps", Modifier.padding(top = 12.dp)) }
            itemsIndexed(state.steps, key = { _, step -> "step:${step.id}" }) { index, step ->
                ReorderableRow(
                    canMoveUp = index > 0,
                    canMoveDown = index < state.steps.lastIndex,
                    onMove = { viewModel.moveStep(step.id, it) },
                    onRemove = { viewModel.removeStep(step.id) },
                    itemLabel = "step ${index + 1}",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Field(step.text, { viewModel.onStepText(step.id, it) }, "Step ${index + 1}", singleLine = false, minLines = 2)
                        Field(
                            step.timerMinutes,
                            { viewModel.onStepTimer(step.id, it) },
                            "Timer (min, optional)",
                            Modifier.width(200.dp),
                            keyboardType = KeyboardType.Number,
                        )
                    }
                }
            }
            item(key = "add-step") {
                JsButton("Add step", onClick = viewModel::addStep, icon = JsIcons.Plus, style = JsButtonStyle.Tonal)
            }

            item(key = "more-header") { SectionLabel("Notes & source", Modifier.padding(top = 12.dp)) }
            item(key = "notes") { Field(state.notes, viewModel::onNotes, "Notes (less salt next time…)", singleLine = false, minLines = 3) }
            item(key = "tags") { Field(state.tags, viewModel::onTags, "Tags, separated by commas") }
            item(key = "source") {
                Field(state.sourceUrl, viewModel::onSourceUrl, "Source link", keyboardType = KeyboardType.Uri, capitalization = KeyboardCapitalization.None)
            }
            if (!state.isNew) {
                item(key = "delete") {
                    Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        JsButton("Delete recipe", onClick = onDelete, style = JsButtonStyle.Text, icon = JsIcons.Trash)
                    }
                }
            }
            item(key = "bottom") { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    if (pasteOpen) {
        PasteIngredientsDialog(onDismiss = { pasteOpen = false }, onPaste = {
            viewModel.pasteIngredients(it)
            pasteOpen = false
        })
    }
    photoMenu?.let { photo ->
        PhotoOptionsDialog(
            photo = photo,
            onDismiss = { photoMenu = null },
            onMakeCover = { viewModel.makeCover(photo.id) },
            onCaption = { viewModel.onCaption(photo.id, it) },
            onRemove = { viewModel.removePhoto(photo.id) },
        )
    }
}

@Composable
private fun PhotoRow(state: EditorUiState, onAddPhotos: () -> Unit, onCamera: () -> Unit, onPhotoClick: (Photo) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "add") { PhotoAction(JsIcons.Image, "Add photos", onAddPhotos) }
        item(key = "camera") { PhotoAction(JsIcons.Camera, "Take a photo", onCamera) }
        items(state.photos, key = { it.id }) { photo ->
            Box(
                Modifier
                    .size(width = 96.dp, height = 120.dp)
                    .clip(JsTheme.shapes.tile)
                    .background(JsTheme.colors.surfaceVariant)
                    .clickable(role = Role.Button, onClickLabel = "Photo options") { onPhotoClick(photo) },
            ) {
                AsyncImage(
                    model = (photo.thumbnailPath ?: photo.localPath)?.let(::File),
                    contentDescription = photo.caption.ifBlank { "Recipe photo" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (photo.isCover) {
                    Text(
                        "Cover",
                        style = JsTheme.typography.labelSmall,
                        color = JsTheme.extendedColors.onBrand,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(JsTheme.shapes.pill)
                            .background(JsTheme.extendedColors.brand)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
        if (state.importingPhotos > 0) {
            item(key = "importing") {
                Box(Modifier.size(width = 96.dp, height = 120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JsTheme.extendedColors.brand)
                }
            }
        }
    }
}

@Composable
private fun PhotoAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier
            .size(width = 96.dp, height = 120.dp)
            .clip(JsTheme.shapes.tile)
            .border(1.5.dp, JsTheme.colors.outline, JsTheme.shapes.tile)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = JsTheme.extendedColors.brandText)
        Text(label, style = JsTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun ReorderableRow(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
    itemLabel: String,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { content() }
        Column {
            JsIconButton(JsIcons.ArrowUp, "Move $itemLabel up", onClick = { onMove(-1) }, enabled = canMoveUp, tint = JsTheme.colors.onSurfaceVariant)
            JsIconButton(JsIcons.ArrowDown, "Move $itemLabel down", onClick = { onMove(1) }, enabled = canMoveDown, tint = JsTheme.colors.onSurfaceVariant)
        }
        JsIconButton(JsIcons.Close, "Remove $itemLabel", onClick = onRemove, tint = JsTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    textStyle: TextStyle = JsTheme.typography.bodyLarge,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        textStyle = textStyle,
        isError = isError,
        shape = JsTheme.shapes.control,
        keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = {
            onImeAction?.invoke()
            defaultKeyboardAction(imeAction)
        }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JsTheme.extendedColors.brandText,
            focusedLabelColor = JsTheme.extendedColors.brandText,
            cursorColor = JsTheme.extendedColors.brandText,
            unfocusedContainerColor = JsTheme.colors.surfaceContainerLowest,
            focusedContainerColor = JsTheme.colors.surfaceContainerLowest,
        ),
    )
}

@Composable
private fun Chip(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
        shape = JsTheme.shapes.pill,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = JsTheme.extendedColors.brand,
            selectedLabelColor = JsTheme.extendedColors.onBrand,
        ),
    )
}

@Composable
private fun Label(text: String) {
    Text(text, style = JsTheme.typography.labelLarge, color = JsTheme.colors.onSurfaceVariant)
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = JsTheme.typography.headlineSmall, modifier = modifier.semantics { heading() })
}

@Composable
private fun PasteIngredientsDialog(onDismiss: () -> Unit, onPaste: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paste ingredients") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("2 cups flour\n1 tsp salt\nFor the glaze:\n1 cup powdered sugar") },
                minLines = 6,
                shape = JsTheme.shapes.control,
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
            )
        },
        confirmButton = { JsButton("Add", onClick = { onPaste(text) }, enabled = text.isNotBlank()) },
        dismissButton = { JsButton("Cancel", onClick = onDismiss, style = JsButtonStyle.Text) },
        shape = JsTheme.shapes.card,
        containerColor = JsTheme.colors.surfaceContainerHigh,
    )
}

@Composable
private fun PhotoOptionsDialog(
    photo: Photo,
    onDismiss: () -> Unit,
    onMakeCover: () -> Unit,
    onCaption: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var caption by rememberSaveable(photo.id) { mutableStateOf(photo.caption) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Photo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = (photo.thumbnailPath ?: photo.localPath)?.let(::File),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(JsTheme.shapes.tile),
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    singleLine = true,
                    shape = JsTheme.shapes.control,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!photo.isCover) {
                        JsButton("Make cover", onClick = {
                            onMakeCover()
                            onDismiss()
                        }, style = JsButtonStyle.Tonal)
                    }
                    JsButton("Remove", onClick = {
                        onRemove()
                        onDismiss()
                    }, style = JsButtonStyle.Text, icon = JsIcons.Trash)
                }
            }
        },
        confirmButton = {
            JsButton("Done", onClick = {
                onCaption(caption)
                onDismiss()
            })
        },
        shape = JsTheme.shapes.card,
        containerColor = JsTheme.colors.surfaceContainerHigh,
    )
}
