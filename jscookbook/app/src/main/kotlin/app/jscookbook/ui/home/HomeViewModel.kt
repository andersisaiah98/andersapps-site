package app.jscookbook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.model.RecipeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val recentlyAdded: List<RecipeSummary> = emptyList(),
    val favorites: List<RecipeSummary> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && recentlyAdded.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(recipes: RecipeRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = recipes.observeRecipes()
        .map { all ->
            HomeUiState(
                loading = false,
                recentlyAdded = all.sortedByDescending { it.createdAt }.take(12),
                favorites = all.filter { it.isFavorite },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
