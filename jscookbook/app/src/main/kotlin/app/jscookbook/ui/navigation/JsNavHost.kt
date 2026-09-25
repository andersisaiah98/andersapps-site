package app.jscookbook.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.jscookbook.core.designsystem.catalog.DesignSystemCatalog
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Category
import app.jscookbook.ui.category.CategoryRecipesRoute
import app.jscookbook.ui.common.LocalNavAnimatedVisibilityScope
import app.jscookbook.ui.common.LocalSharedTransitionScope
import app.jscookbook.ui.common.TileOrigin
import app.jscookbook.ui.cookbook.CookbookRoute
import app.jscookbook.ui.detail.RecipeDetailRoute
import app.jscookbook.ui.editor.RecipeEditorRoute
import app.jscookbook.ui.home.HomeRoute
import app.jscookbook.ui.journal.JournalScreen
import app.jscookbook.ui.photos.PhotoViewerRoute
import app.jscookbook.ui.settings.SettingsRoute

/** Actions that open app-level UI (sheets) rather than destinations. */
class AppActions(
    val editCategory: (Category?) -> Unit,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun JsNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    appActions: AppActions,
    modifier: Modifier = Modifier,
) {
    val motion = JsTheme.motion
    val openRecipe: (String, String) -> Unit = { id, origin -> navController.navigate(Routes.RecipeDetail(id, origin)) }
    val newRecipe: (String?) -> Unit = { categoryId -> navController.navigate(Routes.RecipeEditor(categoryId = categoryId)) }
    val back: () -> Unit = { navController.popBackStack() }

    SharedTransitionLayout(modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Routes.Home,
                // Tabs are peers: a quick fade-through with a slight spring-in scale.
                enterTransition = { fadeIn(motion.gentle()) + scaleIn(motion.gentle(), initialScale = 0.97f) },
                exitTransition = { fadeOut(motion.snappy()) },
                popEnterTransition = { fadeIn(motion.gentle()) + scaleIn(motion.gentle(), initialScale = 0.97f) },
                // Also drives the predictive-back preview: the screen shrinks a touch as it fades.
                popExitTransition = { fadeOut(motion.snappy()) + scaleOut(motion.gentle(), targetScale = 0.97f) },
            ) {
                composable<Routes.Home> {
                    WithNavScope {
                        HomeRoute(
                            contentPadding = contentPadding,
                            onRecipeClick = openRecipe,
                            onNewRecipe = { newRecipe(null) },
                        )
                    }
                }
                composable<Routes.Cookbook> {
                    WithNavScope {
                        CookbookRoute(
                            contentPadding = contentPadding,
                            onRecipeClick = { openRecipe(it, TileOrigin.Cookbook) },
                            onCategoryClick = { navController.navigate(Routes.CategoryRecipes(it.id)) },
                            onNewCategory = { appActions.editCategory(null) },
                            onNewRecipe = { newRecipe(null) },
                        )
                    }
                }
                composable<Routes.Journal> {
                    JournalScreen(contentPadding = contentPadding)
                }
                composable<Routes.Settings> {
                    SettingsRoute(
                        contentPadding = contentPadding,
                        onOpenDesignSystem = { navController.navigate(Routes.DesignSystem) },
                    )
                }
                composable<Routes.DesignSystem>(
                    enterTransition = { slideIntoContainer(SlideDirection.Start, motion.hero()) },
                    popExitTransition = { slideOutOfContainer(SlideDirection.End, motion.hero()) },
                ) {
                    DesignSystemCatalog(onBack = back)
                }
                composable<Routes.CategoryRecipes>(
                    enterTransition = { slideIntoContainer(SlideDirection.Start, motion.hero()) + fadeIn(motion.gentle()) },
                    popExitTransition = { slideOutOfContainer(SlideDirection.End, motion.hero()) + fadeOut(motion.snappy()) },
                ) {
                    WithNavScope {
                        CategoryRecipesRoute(
                            onBack = back,
                            onRecipeClick = { openRecipe(it, TileOrigin.Category) },
                            onEditCategory = { appActions.editCategory(it) },
                            onNewRecipe = { newRecipe(it) },
                        )
                    }
                }
                composable<Routes.RecipeDetail>(
                    // The hero flies in as a shared element; the rest of the page fades up around it.
                    enterTransition = { fadeIn(motion.gentle()) },
                    exitTransition = { fadeOut(motion.snappy()) },
                    popEnterTransition = { fadeIn(motion.gentle()) },
                    popExitTransition = { fadeOut(motion.gentle()) },
                ) {
                    WithNavScope {
                        RecipeDetailRoute(
                            onBack = back,
                            onEdit = { id -> navController.navigate(Routes.RecipeEditor(recipeId = id)) },
                            onOpenPhoto = { id, index -> navController.navigate(Routes.PhotoViewer(id, index)) },
                            onOpenCategory = { navController.navigate(Routes.CategoryRecipes(it)) },
                        )
                    }
                }
                composable<Routes.RecipeEditor>(
                    enterTransition = { slideIntoContainer(SlideDirection.Up, motion.gentle()) + fadeIn(motion.gentle()) },
                    popExitTransition = { slideOutOfContainer(SlideDirection.Down, motion.gentle()) + fadeOut(motion.snappy()) },
                ) {
                    RecipeEditorRoute(
                        onClose = back,
                        onSaved = { id, isNew ->
                            if (isNew) {
                                navController.navigate(Routes.RecipeDetail(id, TileOrigin.Editor)) {
                                    popUpTo<Routes.RecipeEditor> { inclusive = true }
                                }
                            } else {
                                back()
                            }
                        },
                        onDeleted = {
                            // Leave both the editor and the (now deleted) recipe's page.
                            navController.popBackStack<Routes.RecipeDetail>(inclusive = true)
                                .takeIf { it } ?: navController.popBackStack()
                        },
                        onNewCategory = { appActions.editCategory(null) },
                    )
                }
                composable<Routes.PhotoViewer>(
                    enterTransition = { fadeIn(motion.gentle()) + scaleIn(motion.gentle(), initialScale = 0.92f) },
                    popExitTransition = { fadeOut(motion.snappy()) },
                ) {
                    PhotoViewerRoute(onClose = back)
                }
            }
        }
    }
}

@Composable
private fun AnimatedContentScope.WithNavScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}
