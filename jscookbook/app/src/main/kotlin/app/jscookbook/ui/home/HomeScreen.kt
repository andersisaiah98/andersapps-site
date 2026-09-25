package app.jscookbook.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.component.QuickActionCard
import app.jscookbook.core.designsystem.component.RecipeTile
import app.jscookbook.core.designsystem.component.SectionHeader
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.LocalRecipeImageProvider
import app.jscookbook.core.designsystem.recipeimage.RecipeArt
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.ui.common.StaggeredEntrance
import app.jscookbook.ui.common.rememberEntrance
import app.jscookbook.ui.common.screenPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.model.RecipeSummary
import app.jscookbook.core.model.formatMinutes
import app.jscookbook.ui.common.LocalMessenger
import app.jscookbook.ui.common.TileOrigin
import app.jscookbook.ui.common.imageRequest
import app.jscookbook.ui.common.recipeImageSharedElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class QuickAction(val comingSoon: String) {
    RandomMeal("The randomizer arrives in Phase 3."),
    Snack("Snack picks arrive in Phase 3."),
    QuickOne("Quick picks arrive in Phase 3."),
    NotInAWhile("Forgotten favorites arrive in Phase 3."),
}

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    onRecipeClick: (id: String, origin: String) -> Unit,
    onNewRecipe: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    HomeScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onQuickAction = { messenger.show(it.comingSoon) },
        onRecipeClick = onRecipeClick,
        onNewRecipe = onNewRecipe,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onQuickAction: (QuickAction) -> Unit,
    onRecipeClick: (id: String, origin: String) -> Unit,
    onNewRecipe: () -> Unit,
    modifier: Modifier = Modifier,
    now: LocalDateTime = remember { LocalDateTime.now() },
) {
    val padding = screenPadding(contentPadding)
    val entrance = rememberEntrance()
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("screen:home"),
        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item(key = "greeting") {
            StaggeredEntrance(entrance, index = 0, modifier = Modifier.padding(horizontal = 20.dp)) {
                Greeting(now)
            }
        }
        if (uiState.isEmpty) {
            item(key = "empty") {
                StaggeredEntrance(entrance, index = 1, modifier = Modifier.padding(horizontal = 20.dp)) {
                    FirstRecipeCard(onNewRecipe)
                }
            }
        }
        item(key = "quick") {
            StaggeredEntrance(entrance, index = 2, modifier = Modifier.padding(horizontal = 20.dp)) {
                QuickActions(onQuickAction)
            }
        }
        if (uiState.recentlyAdded.isNotEmpty()) {
            item(key = "recent") {
                StaggeredEntrance(entrance, index = 3) {
                    RecentlyAdded(uiState.recentlyAdded) { onRecipeClick(it, TileOrigin.HomeRecent) }
                }
            }
        }
        if (uiState.favorites.isNotEmpty()) {
            item(key = "favorites") {
                StaggeredEntrance(entrance, index = 4) {
                    Favorites(uiState.favorites) { onRecipeClick(it, TileOrigin.HomeFavorites) }
                }
            }
        }
    }
}

@Composable
private fun FirstRecipeCard(onNewRecipe: () -> Unit) {
    JsCard(modifier = Modifier.fillMaxWidth(), color = JsTheme.colors.surfaceContainer) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Start the cook book", style = JsTheme.typography.titleLarge)
                Text(
                    "Add a favorite recipe. Photos are optional; every recipe gets its own art.",
                    style = JsTheme.typography.bodyMedium,
                    color = JsTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                JsButton("Add a recipe", onClick = onNewRecipe, icon = JsIcons.Plus)
            }
            Image(
                imageVector = FoodIllustration.Loaf.vector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(JsTheme.extendedColors.brandText),
                modifier = Modifier.padding(start = 12.dp).size(88.dp),
            )
        }
    }
}

@Composable
private fun Greeting(now: LocalDateTime) {
    val brandText = JsTheme.extendedColors.brandText
    val date = remember(now) {
        now.format(DateTimeFormatter.ofPattern("EEEE · MMMM d", Locale.getDefault())).uppercase(Locale.getDefault())
    }
    Column {
        Text(
            date,
            style = JsTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
            color = JsTheme.colors.onSurfaceVariant,
        )
        Text(
            buildAnnotatedString {
                append("Hi, ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = brandText)) { append("Julia") }
            },
            style = JsTheme.typography.displayMedium,
            modifier = Modifier.padding(top = 6.dp).semantics { heading() },
        )
        Text(
            promptFor(now.hour),
            style = JsTheme.typography.bodyLarge,
            color = JsTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun promptFor(hour: Int): String = when (hour) {
    in 5..10 -> "Something good for breakfast?"
    in 11..14 -> "What sounds good for lunch?"
    in 15..20 -> "What's for dinner tonight?"
    else -> "A little something before bed?"
}

@Composable
private fun QuickActions(onQuickAction: (QuickAction) -> Unit) {
    val dark = JsTheme.extendedColors.isDark
    // With very large text the half-width cards would be cramped, so they stack instead.
    val stack = LocalDensity.current.fontScale >= 1.5f
    val snack: @Composable (Modifier) -> Unit = { modifier ->
        QuickActionCard(
            title = "Give me a snack to make",
            onClick = { onQuickAction(QuickAction.Snack) },
            illustration = FoodIllustration.Cookie,
            palette = CardPalettes.MexicanTile,
            icon = JsIcons.Cookie,
            modifier = modifier,
        )
    }
    val quick: @Composable (Modifier) -> Unit = { modifier ->
        QuickActionCard(
            title = "Quick one",
            subtitle = "Under 30 min",
            onClick = { onQuickAction(QuickAction.QuickOne) },
            illustration = FoodIllustration.Skillet,
            palette = CardPalettes.PinkBeige,
            icon = JsIcons.Bolt,
            modifier = modifier,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionCard(
            title = "Give me a random meal to make",
            subtitle = "Skips anything from the last week",
            onClick = { onQuickAction(QuickAction.RandomMeal) },
            illustration = FoodIllustration.Plate,
            palette = CardPalettes.TerraCotta,
            icon = JsIcons.Dice,
            large = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (stack) {
            snack(Modifier.fillMaxWidth())
            quick(Modifier.fillMaxWidth())
        } else {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                snack(Modifier.weight(1f).fillMaxHeight())
                quick(Modifier.weight(1f).fillMaxHeight())
            }
        }
        QuickActionCard(
            title = "Something I haven't made in a while",
            onClick = { onQuickAction(QuickAction.NotInAWhile) },
            illustration = FoodIllustration.Loaf,
            palette = if (dark) CardPalettes.Portico else CardPalettes.Rustique,
            icon = JsIcons.Hourglass,
            large = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RecentlyAdded(recipes: List<RecipeSummary>, onRecipeClick: (String) -> Unit) {
    val provider = LocalRecipeImageProvider.current
    Column {
        SectionHeader(title = "Recently added", modifier = Modifier.padding(horizontal = 20.dp))
        LazyRow(
            modifier = Modifier.padding(top = 14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(recipes, key = { it.id }) { recipe ->
                RecipeTile(
                    title = recipe.title,
                    image = remember(recipe) { provider.imageFor(recipe.imageRequest()) },
                    note = recipe.totalMinutes?.let(::formatMinutes) ?: recipe.type?.label,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.width(148.dp),
                    imageModifier = Modifier.recipeImageSharedElement(recipe.id, TileOrigin.HomeRecent),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Favorites(favorites: List<RecipeSummary>, onRecipeClick: (String) -> Unit) {
    val provider = LocalRecipeImageProvider.current
    Column {
        SectionHeader(
            title = "Favorites",
            subtitle = if (favorites.size > 1) "Swipe through" else null,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { favorites.size },
            preferredItemWidth = 232.dp,
            itemSpacing = 10.dp,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.padding(top = 14.dp).fillMaxWidth().height(272.dp),
        ) { index ->
            val recipe = favorites[index]
            Box(
                Modifier
                    .fillMaxSize()
                    .maskClip(JsTheme.shapes.tile)
                    .clickable(role = Role.Button, onClickLabel = "Open ${recipe.title}") { onRecipeClick(recipe.id) },
            ) {
                RecipeArt(
                    image = remember(recipe) { provider.imageFor(recipe.imageRequest()) },
                    modifier = Modifier.fillMaxSize().recipeImageSharedElement(recipe.id, TileOrigin.HomeFavorites),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
                        .graphicsLayer {
                            // Fade the title out as the item shrinks toward the carousel's edge.
                            val range = carouselItemDrawInfo.maxSize - carouselItemDrawInfo.minSize
                            alpha = if (range <= 0f) 1f else ((carouselItemDrawInfo.size - carouselItemDrawInfo.minSize) / range).coerceIn(0f, 1f)
                        }
                        .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
                ) {
                    Text(
                        recipe.title,
                        style = JsTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
