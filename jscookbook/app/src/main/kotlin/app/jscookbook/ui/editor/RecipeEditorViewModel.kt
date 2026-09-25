package app.jscookbook.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.jscookbook.core.data.photo.PhotoStore
import app.jscookbook.core.data.repository.CategoryRepository
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.data.repository.newId
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.Ingredient
import app.jscookbook.core.model.IngredientParser
import app.jscookbook.core.model.ParsedIngredientLine
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.Recipe
import app.jscookbook.core.model.RecipeDraft
import app.jscookbook.core.model.RecipeType
import app.jscookbook.core.model.Step
import app.jscookbook.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** One ingredient as typed, e.g. "2 cups flour, sifted", or a heading like "For the sauce:". */
data class IngredientLine(val id: String = newId(), val text: String = "")

data class StepLine(val id: String = newId(), val text: String = "", val timerMinutes: String = "")

data class EditorUiState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val id: String = "",
    val title: String = "",
    val type: RecipeType? = null,
    val description: String = "",
    val servings: String = "",
    val prepMinutes: String = "",
    val cookMinutes: String = "",
    val sourceUrl: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val rating: Int? = null,
    val notes: String = "",
    val categoryIds: Set<String> = emptySet(),
    val ingredients: List<IngredientLine> = listOf(IngredientLine()),
    val steps: List<StepLine> = listOf(StepLine()),
    val photos: List<Photo> = emptyList(),
    val importingPhotos: Int = 0,
    val dirty: Boolean = false,
    val saving: Boolean = false,
    val showTitleError: Boolean = false,
)

sealed interface EditorEvent {
    data class Saved(val id: String, val isNew: Boolean) : EditorEvent
    data object Deleted : EditorEvent
    data class Message(val text: String) : EditorEvent
}

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipes: RecipeRepository,
    categoryRepository: CategoryRepository,
    private val photoStore: PhotoStore,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Routes.RecipeEditor>()

    /** The camera should open once when the editor was started from "Snap a photo". */
    var cameraPending: Boolean = route.camera && route.recipeId == null
        private set

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = MutableSharedFlow<EditorEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    /** Photos imported during this session, so a discarded editor doesn't leave files behind. */
    private val importedThisSession = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            val existing = route.recipeId?.let { recipes.observeRecipe(it).first() }
            _state.value = if (existing != null) {
                existing.toEditorState()
            } else {
                EditorUiState(
                    loading = false,
                    isNew = true,
                    id = route.recipeId ?: newId(),
                    categoryIds = setOfNotNull(route.categoryId),
                )
            }
        }
    }

    fun cameraHandled() {
        cameraPending = false
    }

    private fun edit(block: (EditorUiState) -> EditorUiState) = _state.update { block(it).copy(dirty = true) }

    fun onTitle(value: String) = edit { it.copy(title = value, showTitleError = false) }
    fun onType(value: RecipeType?) = edit { it.copy(type = value) }
    fun onDescription(value: String) = edit { it.copy(description = value) }
    fun onServings(value: String) = edit { it.copy(servings = value) }
    fun onPrep(value: String) = edit { it.copy(prepMinutes = value.filter(Char::isDigit).take(4)) }
    fun onCook(value: String) = edit { it.copy(cookMinutes = value.filter(Char::isDigit).take(4)) }
    fun onSourceUrl(value: String) = edit { it.copy(sourceUrl = value) }
    fun onTags(value: String) = edit { it.copy(tags = value) }
    fun onFavorite(value: Boolean) = edit { it.copy(isFavorite = value) }
    fun onRating(value: Int?) = edit { it.copy(rating = value) }
    fun onNotes(value: String) = edit { it.copy(notes = value) }

    fun toggleCategory(id: String) = edit {
        it.copy(categoryIds = if (id in it.categoryIds) it.categoryIds - id else it.categoryIds + id)
    }

    // Ingredients

    fun onIngredientText(id: String, text: String) = edit { s ->
        s.copy(ingredients = s.ingredients.map { if (it.id == id) it.copy(text = text) else it })
    }

    fun addIngredient(afterId: String? = null) = edit { s ->
        val index = afterId?.let { id -> s.ingredients.indexOfFirst { it.id == id } }?.takeIf { it >= 0 }
        val list = s.ingredients.toMutableList()
        list.add(index?.plus(1) ?: list.size, IngredientLine())
        s.copy(ingredients = list)
    }

    fun removeIngredient(id: String) = edit { s -> s.copy(ingredients = s.ingredients.filterNot { it.id == id }) }

    fun moveIngredient(id: String, by: Int) = edit { s -> s.copy(ingredients = s.ingredients.moved(id, by) { it.id }) }

    /** A pasted list: one ingredient per line, headings ending in ":". */
    fun pasteIngredients(text: String) = edit { s ->
        val pasted = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { IngredientLine(text = it) }
        s.copy(ingredients = s.ingredients.filter { it.text.isNotBlank() } + pasted)
    }

    // Steps

    fun onStepText(id: String, text: String) = edit { s -> s.copy(steps = s.steps.map { if (it.id == id) it.copy(text = text) else it }) }

    fun onStepTimer(id: String, minutes: String) = edit { s ->
        s.copy(steps = s.steps.map { if (it.id == id) it.copy(timerMinutes = minutes.filter(Char::isDigit).take(3)) else it })
    }

    fun addStep() = edit { s -> s.copy(steps = s.steps + StepLine()) }

    fun removeStep(id: String) = edit { s -> s.copy(steps = s.steps.filterNot { it.id == id }) }

    fun moveStep(id: String, by: Int) = edit { s -> s.copy(steps = s.steps.moved(id, by) { it.id }) }

    // Photos

    fun newCaptureFile(): File = photoStore.newCaptureFile()

    fun addPhotos(uris: List<Uri>, cleanup: (() -> Unit)? = null) {
        if (uris.isEmpty()) return
        _state.update { it.copy(importingPhotos = it.importingPhotos + uris.size) }
        viewModelScope.launch {
            uris.forEach { uri ->
                runCatching { photoStore.import(uri) }
                    .onSuccess { photo ->
                        importedThisSession += photo.id
                        edit { s ->
                            val first = s.photos.isEmpty()
                            s.copy(photos = s.photos + photo.copy(isCover = first))
                        }
                    }
                    .onFailure { _events.tryEmit(EditorEvent.Message("Couldn't add that photo")) }
                _state.update { it.copy(importingPhotos = (it.importingPhotos - 1).coerceAtLeast(0)) }
            }
            cleanup?.invoke()
        }
    }

    fun makeCover(id: String) = edit { s -> s.copy(photos = s.photos.map { it.copy(isCover = it.id == id) }) }

    fun onCaption(id: String, caption: String) = edit { s -> s.copy(photos = s.photos.map { if (it.id == id) it.copy(caption = caption) else it }) }

    fun removePhoto(id: String) = edit { s ->
        val remaining = s.photos.filterNot { it.id == id }
        val hasCover = remaining.any { it.isCover }
        s.copy(photos = remaining.mapIndexed { i, p -> if (!hasCover && i == 0) p.copy(isCover = true) else p })
    }

    // Save / discard / delete

    fun save() {
        val s = _state.value
        if (s.saving || s.loading) return
        if (s.title.isBlank()) {
            _state.update { it.copy(showTitleError = true) }
            _events.tryEmit(EditorEvent.Message("Give the recipe a name first"))
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            recipes.save(s.toDraft())
            importedThisSession.clear()
            _state.update { it.copy(saving = false, dirty = false) }
            _events.emit(EditorEvent.Saved(s.id, s.isNew))
        }
    }

    /** Deletes photo files imported in this session that were never saved. */
    fun discard() {
        val unsaved = _state.value.photos.filter { it.id in importedThisSession }
        viewModelScope.launch { unsaved.forEach { photoStore.discard(it) } }
        importedThisSession.clear()
    }

    fun delete() {
        viewModelScope.launch {
            recipes.delete(_state.value.id)
            _events.emit(EditorEvent.Deleted)
        }
    }

    fun restore(id: String) {
        viewModelScope.launch { recipes.restore(id) }
    }
}

private fun <T> List<T>.moved(id: String, by: Int, key: (T) -> String): List<T> {
    val from = indexOfFirst { key(it) == id }
    val to = (from + by).coerceIn(0, lastIndex)
    if (from < 0 || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

internal fun Recipe.toEditorState(): EditorUiState {
    val lines = mutableListOf<IngredientLine>()
    var section: String? = null
    ingredients.forEach { ingredient ->
        if (ingredient.section != null && ingredient.section != section) {
            lines += IngredientLine(text = "${ingredient.section}:")
        }
        section = ingredient.section
        lines += IngredientLine(
            id = ingredient.id,
            text = IngredientParser.format(ingredient.quantity, ingredient.unit, ingredient.name, ingredient.note),
        )
    }
    return EditorUiState(
        loading = false,
        isNew = false,
        id = id,
        title = title,
        type = type,
        description = description,
        servings = servings,
        prepMinutes = prepMinutes?.toString().orEmpty(),
        cookMinutes = cookMinutes?.toString().orEmpty(),
        sourceUrl = sourceUrl,
        tags = tags.joinToString(", "),
        isFavorite = isFavorite,
        rating = rating,
        notes = notes,
        categoryIds = categories.map { it.id }.toSet(),
        ingredients = lines.ifEmpty { listOf(IngredientLine()) },
        steps = steps.map { StepLine(it.id, it.text, it.timerSeconds?.let { s -> (s / 60).toString() }.orEmpty()) }
            .ifEmpty { listOf(StepLine()) },
        photos = photos,
    )
}

/** Turns the typed lines back into structured rows; headings become each item's section. */
internal fun EditorUiState.toDraft(): RecipeDraft {
    var section: String? = null
    val parsedIngredients = ingredients.filter { it.text.isNotBlank() }.mapNotNull { line ->
        when (val parsed = IngredientParser.parse(line.text)) {
            is ParsedIngredientLine.Section -> {
                section = parsed.title
                null
            }
            is ParsedIngredientLine.Item -> Ingredient(line.id, section, parsed.quantity, parsed.unit, parsed.name, parsed.note)
        }
    }
    return RecipeDraft(
        id = id,
        title = title,
        type = type,
        description = description,
        servings = servings,
        prepMinutes = prepMinutes.toIntOrNull(),
        cookMinutes = cookMinutes.toIntOrNull(),
        sourceUrl = sourceUrl,
        tags = tags.split(',', '#').map { it.trim() }.filter { it.isNotEmpty() },
        isFavorite = isFavorite,
        rating = rating,
        notes = notes,
        categoryIds = categoryIds,
        ingredients = parsedIngredients,
        steps = steps.filter { it.text.isNotBlank() }.map { Step(it.id, it.text, it.timerMinutes.toIntOrNull()?.times(60)) },
        photos = photos,
    )
}
