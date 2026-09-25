package app.jscookbook.ui.cookbook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.SectionHeader
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.modifier.springyPress
import app.jscookbook.core.designsystem.recipeimage.RecipeType
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.softShadow
import app.jscookbook.ui.common.ScreenTitle
import app.jscookbook.ui.common.screenPadding
import app.jscookbook.ui.home.SampleCategories
import app.jscookbook.ui.home.SampleCategory

@Composable
fun CookbookScreen(
    contentPadding: PaddingValues,
    onCategoryClick: (SampleCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<RecipeType?>(null) }
    val categories = SampleCategories.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    val padding = screenPadding(contentPadding)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().testTag("screen:cookbook"),
        contentPadding = padding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title", span = { GridItemSpan(maxLineSpan) }) {
            ScreenTitle("Cookbook", subtitle = "Everything Julia cooks, on shelves.")
        }
        item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
            SearchField(query = query, onQueryChange = { query = it }, modifier = Modifier.padding(top = 8.dp))
        }
        item(key = "types", span = { GridItemSpan(maxLineSpan) }) {
            TypeFilters(selected = type, onSelect = { type = it })
        }
        item(key = "categories-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = "Categories",
                subtitle = "Samples until you add your own",
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        items(categories, key = { it.name }) { category ->
            CategoryCard(category, onClick = { onCategoryClick(category) })
        }
        if (categories.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "No categories match “${query.trim()}”.",
                    style = JsTheme.typography.bodyLarge,
                    color = JsTheme.colors.onSurfaceVariant,
                )
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
        placeholder = { Text("Search recipes, ingredients, tags") },
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

@Composable
private fun CategoryCard(category: SampleCategory, onClick: () -> Unit) {
    val palette = category.palette
    val shape = JsTheme.shapes.tile
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxWidth()
            .springyPress(interactionSource)
            .softShadow(JsElevations.Resting, shape, JsTheme.extendedColors.shadow)
            .clip(shape)
            .background(palette.container)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = palette.content),
                role = Role.Button,
                onClick = onClick,
            )
            .heightIn(min = 156.dp)
            .padding(16.dp),
    ) {
        Image(
            imageVector = category.illustration.vector,
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.ink),
            modifier = Modifier.size(64.dp).align(Alignment.End),
        )
        Spacer(Modifier.weight(1f))
        Text(category.name, style = JsTheme.typography.titleLarge, color = palette.content)
        Text(
            "${category.count} recipes",
            style = JsTheme.typography.labelLarge,
            color = palette.content,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
