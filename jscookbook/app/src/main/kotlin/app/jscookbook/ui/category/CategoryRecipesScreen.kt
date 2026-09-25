package app.jscookbook.ui.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsIconButton
import app.jscookbook.core.designsystem.component.RecipeTile
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.LocalRecipeImageProvider
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Category
import app.jscookbook.core.model.formatMinutes
import app.jscookbook.ui.common.TileOrigin
import app.jscookbook.ui.common.imageRequest
import app.jscookbook.ui.common.recipeImageSharedElement

@Composable
fun CategoryRecipesRoute(
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onEditCategory: (Category) -> Unit,
    onNewRecipe: (categoryId: String) -> Unit,
    viewModel: CategoryRecipesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoryRecipesScreen(
        uiState = uiState,
        onBack = onBack,
        onRecipeClick = onRecipeClick,
        onEditCategory = onEditCategory,
        onNewRecipe = { onNewRecipe(viewModel.categoryId) },
    )
}

@Composable
fun CategoryRecipesScreen(
    uiState: CategoryRecipesUiState,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onEditCategory: (Category) -> Unit,
    onNewRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val provider = LocalRecipeImageProvider.current
    val category = uiState.category
    val palette = CardPalettes.fromKey(category?.colorKey.orEmpty())
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().testTag("screen:category"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = top + 4.dp, bottom = bottom + 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "bar", span = { GridItemSpan(maxLineSpan) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                JsIconButton(JsIcons.Back, contentDescription = "Back", onClick = onBack)
                Box(Modifier.weight(1f))
                if (category != null) {
                    JsIconButton(JsIcons.Pencil, contentDescription = "Edit category", onClick = { onEditCategory(category) })
                }
            }
        }
        item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(JsTheme.shapes.card)
                    .background(palette.container)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        category?.name.orEmpty(),
                        style = JsTheme.typography.headlineMedium,
                        color = palette.content,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        when (val n = uiState.recipes.size) { 1 -> "1 recipe"; else -> "$n recipes" },
                        style = JsTheme.typography.bodyMedium,
                        color = palette.content,
                    )
                }
                Image(
                    imageVector = FoodIllustration.fromKey(category?.iconKey.orEmpty()).vector,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(palette.ink),
                    modifier = Modifier.size(72.dp),
                )
            }
        }
        items(uiState.recipes, key = { it.id }) { recipe ->
            RecipeTile(
                title = recipe.title,
                image = remember(recipe) { provider.imageFor(recipe.imageRequest()) },
                note = recipe.totalMinutes?.let(::formatMinutes) ?: recipe.type?.label,
                onClick = { onRecipeClick(recipe.id) },
                imageModifier = Modifier.recipeImageSharedElement(recipe.id, TileOrigin.Category),
            )
        }
        item(key = "add", span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                JsButton(
                    if (uiState.recipes.isEmpty() && !uiState.loading) "Add the first recipe here" else "Add a recipe here",
                    onClick = onNewRecipe,
                    icon = JsIcons.Plus,
                    style = JsButtonStyle.Tonal,
                )
            }
        }
    }
}
