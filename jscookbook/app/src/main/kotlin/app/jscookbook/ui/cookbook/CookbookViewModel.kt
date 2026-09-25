package app.jscookbook.ui.cookbook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jscookbook.core.data.repository.CategoryRepository
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.RecipeSummary
import app.jscookbook.core.model.RecipeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CookbookUiState(
    val loading: Boolean = true,
    val query: String = "",
    val type: RecipeType? = null,
    val categories: List<Category> = emptyList(),
    val recipes: List<RecipeSummary> = emptyList(),
    val totalRecipes: Int = 0,
) {
    val filtering: Boolean get() = query.isNotBlank() || type != null
}

@HiltViewModel
class CookbookViewModel @Inject constructor(
    recipes: RecipeRepository,
    categories: CategoryRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val query = savedState.getStateFlow("query", "")
    private val type = savedState.getStateFlow<String?>("type", null)

    val uiState: StateFlow<CookbookUiState> = combine(
        recipes.observeRecipes(),
        categories.observeCategories(),
        query,
        type,
    ) { allRecipes, allCategories, q, typeName ->
        val selectedType = RecipeType.entries.firstOrNull { it.name == typeName }
        val needle = q.trim()
        CookbookUiState(
            loading = false,
            query = q,
            type = selectedType,
            categories = if (needle.isEmpty()) allCategories else allCategories.filter { it.name.contains(needle, ignoreCase = true) },
            recipes = allRecipes
                .filter { selectedType == null || it.type == selectedType }
                .filter { r ->
                    needle.isEmpty() || r.title.contains(needle, ignoreCase = true) ||
                        r.categoryNames.any { it.contains(needle, ignoreCase = true) }
                }
                .sortedBy { it.title.lowercase() },
            totalRecipes = allRecipes.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookbookUiState())

    fun onQueryChange(value: String) {
        savedState["query"] = value
    }

    fun onTypeChange(value: RecipeType?) {
        savedState["type"] = value?.name
    }
}
