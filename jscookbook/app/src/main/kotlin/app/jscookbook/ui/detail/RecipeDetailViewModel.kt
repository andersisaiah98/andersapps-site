package app.jscookbook.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.model.Recipe
import app.jscookbook.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data object Missing : RecipeDetailUiState
    data class Ready(val recipe: Recipe) : RecipeDetailUiState
}

data class StepTimer(val totalSeconds: Int, val remainingSeconds: Int, val running: Boolean)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipes: RecipeRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Routes.RecipeDetail>()
    val recipeId: String = route.recipeId
    val origin: String = route.origin

    val uiState: StateFlow<RecipeDetailUiState> = recipes.observeRecipe(recipeId)
        .map { recipe -> recipe?.let { RecipeDetailUiState.Ready(it) } ?: RecipeDetailUiState.Missing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState.Loading)

    /** Ingredients ticked off while cooking. Session only, on purpose. */
    private val _checked = MutableStateFlow<Set<String>>(emptySet())
    val checked: StateFlow<Set<String>> = _checked.asStateFlow()

    private val _timers = MutableStateFlow<Map<String, StepTimer>>(emptyMap())
    val timers: StateFlow<Map<String, StepTimer>> = _timers.asStateFlow()
    private val timerJobs = mutableMapOf<String, Job>()

    private val _timerDone = MutableSharedFlow<Int>(extraBufferCapacity = 4)
    /** Emits the step number (1-based) whose timer just finished. */
    val timerDone: SharedFlow<Int> = _timerDone.asSharedFlow()

    fun toggleIngredient(id: String) = _checked.update { if (id in it) it - id else it + id }

    fun toggleFavorite() {
        val recipe = (uiState.value as? RecipeDetailUiState.Ready)?.recipe ?: return
        viewModelScope.launch { recipes.setFavorite(recipe.id, !recipe.isFavorite) }
    }

    fun delete() {
        viewModelScope.launch { recipes.delete(recipeId) }
    }

    fun restore() {
        viewModelScope.launch { recipes.restore(recipeId) }
    }

    /** Start, pause or resume a step's countdown; a finished timer restarts. */
    fun toggleTimer(stepId: String, stepNumber: Int, seconds: Int) {
        val current = _timers.value[stepId]
        when {
            current == null || current.remainingSeconds == 0 -> start(stepId, stepNumber, StepTimer(seconds, seconds, true))
            current.running -> {
                timerJobs.remove(stepId)?.cancel()
                _timers.update { it + (stepId to current.copy(running = false)) }
            }
            else -> start(stepId, stepNumber, current.copy(running = true))
        }
    }

    fun resetTimer(stepId: String) {
        timerJobs.remove(stepId)?.cancel()
        _timers.update { it - stepId }
    }

    private fun start(stepId: String, stepNumber: Int, timer: StepTimer) {
        timerJobs.remove(stepId)?.cancel()
        _timers.update { it + (stepId to timer) }
        timerJobs[stepId] = viewModelScope.launch {
            var remaining = timer.remainingSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1
                _timers.update { it + (stepId to timer.copy(remainingSeconds = remaining, running = remaining > 0)) }
            }
            _timerDone.emit(stepNumber)
        }
    }
}
