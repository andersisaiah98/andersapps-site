package app.jscookbook.ui.cookbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.RecipeTile
import app.jscookbook.core.designsystem.component.SectionHeader
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.LocalRecipeImageProvider
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.RecipeType
import app.jscookbook.core.model.formatMinutes
import app.jscookbook.ui.category.CategoryCard
import app.jscookbook.ui.category.NewCategoryCard
import app.jscookbook.ui.common.ScreenTitle
import app.jscookbook.ui.common.TileOrigin
import app.jscookbook.ui.common.imageRequest
import app.jscookbook.ui.common.recipeImageSharedElement
import app.jscookbook.ui.common.screenPadding

@Composable
fun CookbookRoute(
    contentPadding: PaddingValues,
    onRecipeClick: (String) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onNewCategory: () -> Unit,
    onNewRecipe: () -> Unit,
    viewModel: CookbookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CookbookScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onQueryChange = viewModel::onQueryChange,
        onTypeChange = viewModel::onTypeChange,
        onRecipeClick = onRecipeClick,
        onCategoryClick = onCategoryClick,
        onNewCategory = onNewCategory,
        onNewRecipe = onNewRecipe,
    )
}

@Composable
fun CookbookScreen(
    uiState: CookbookUiState,
    contentPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onTypeChange: (RecipeType?) -> Unit,
    onRecipeClick: (String) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onNewCategory: () -> Unit,
    onNewRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val provider = LocalRecipeImageProvider.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().testTag("screen:cookbook"),
        contentPadding = screenPadding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title", span = { GridItemSpan(maxLineSpan) }) {
            ScreenTitle("Cookbook", subtitle = "Everything Julia cooks, on shelves.")
        }
        item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
            SearchField(query = uiState.query, onQueryChange = onQueryChange, modifier = Modifier.padding(top = 8.dp))
        }
        item(key = "types", span = { GridItemSpan(maxLineSpan) }) {
            TypeFilters(selected = uiState.type, onSelect = onTypeChange)
        }

        if (uiState.type == null) {
            item(key = "categories-header", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Categories", modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            }
            items(uiState.categories, key = { "category:" + it.id }) { category ->
                CategoryCard(category, onClick = { onCategoryClick(category) })
            }
            if (!uiState.filtering) {
                item(key = "new-category") { NewCategoryCard(onClick = onNewCategory) }
            }
        }

        item(key = "recipes-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = if (uiState.filtering) "Matching recipes" else "All recipes",
                subtitle = if (uiState.totalRecipes > 0) "${uiState.recipes.size} of ${uiState.totalRecipes}" else null,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
        }
        items(uiState.recipes, key = { "recipe:" + it.id }) { recipe ->
            RecipeTile(
                title = recipe.title,
                image = remember(recipe) { provider.imageFor(recipe.imageRequest()) },
                note = recipe.totalMinutes?.let(::formatMinutes) ?: recipe.type?.label,
                onClick = { onRecipeClick(recipe.id) },
                imageModifier = Modifier.recipeImageSharedElement(recipe.id, TileOrigin.Cookbook),
            )
        }
        if (!uiState.loading && uiState.recipes.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                if (uiState.filtering) {
                    Text(
                        "No recipes match. Try another word or type.",
                        style = JsTheme.typography.bodyLarge,
                        color = JsTheme.colors.onSurfaceVariant,
                    )
                } else {
                    JsButton("Add the first recipe", onClick = onNewRecipe, icon = JsIcons.Plus, style = JsButtonStyle.Tonal)
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = JsTheme.colors
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search recipes and categories") },
        leadingIcon = { Icon(JsIcons.Search, contentDescription = null) },
        singleLine = true,
        shape = JsTheme.shapes.pill,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainer,
            unfocusedContainerColor = colors.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = JsTheme.extendedColors.brandText,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            focusedPlaceholderColor = colors.onSurfaceVariant,
            unfocusedPlaceholderColor = colors.onSurfaceVariant,
        ),
    )
}

@Composable
private fun TypeFilters(selected: RecipeType?, onSelect: (RecipeType?) -> Unit) {
    val options = remember { listOf<RecipeType?>(null) + RecipeType.entries }
    val extended = JsTheme.extendedColors
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { it?.name ?: "all" }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option?.label ?: "All") },
                shape = JsTheme.shapes.pill,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = extended.brand,
                    selectedLabelColor = extended.onBrand,
                ),
            )
        }
    }
}
