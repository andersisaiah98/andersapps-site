package app.jscookbook.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.jscookbook.core.data.repository.CategoryRepository
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.RecipeSummary
import app.jscookbook.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryRecipesUiState(
    val loading: Boolean = true,
    val category: Category? = null,
    val recipes: List<RecipeSummary> = emptyList(),
)

@HiltViewModel
class CategoryRecipesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    categories: CategoryRepository,
    recipes: RecipeRepository,
) : ViewModel() {
    val categoryId = savedStateHandle.toRoute<Routes.CategoryRecipes>().categoryId

    val uiState: StateFlow<CategoryRecipesUiState> = combine(
        categories.observeCategories(),
        recipes.observeRecipesInCategory(categoryId),
    ) { all, inCategory ->
        CategoryRecipesUiState(
            loading = false,
            category = all.firstOrNull { it.id == categoryId },
            recipes = inCategory.sortedBy { it.title.lowercase() },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryRecipesUiState())
}

/** Create, edit and delete categories from the app-level sheet. */
@HiltViewModel
class CategoryActionsViewModel @Inject constructor(
    private val categories: CategoryRepository,
) : ViewModel() {
    fun save(id: String?, name: String, iconKey: String, colorKey: String, onSaved: (String) -> Unit = {}) {
        viewModelScope.launch { onSaved(categories.save(id, name, iconKey, colorKey)) }
    }

    fun delete(id: String) {
        viewModelScope.launch { categories.delete(id) }
    }

    fun restore(id: String) {
        viewModelScope.launch { categories.restore(id) }
    }
}
