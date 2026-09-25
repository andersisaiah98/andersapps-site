package app.jscookbook.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.jscookbook.core.designsystem.catalog.DesignSystemCatalog
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.ui.cookbook.CookbookScreen
import app.jscookbook.ui.home.HomeScreen
import app.jscookbook.ui.journal.JournalScreen
import app.jscookbook.ui.settings.SettingsRoute

@Composable
fun JsNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    showMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = JsTheme.motion
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier,
        // Tabs are peers: a quick fade-through with a slight spring-in scale.
        enterTransition = { fadeIn(motion.gentle()) + scaleIn(motion.gentle(), initialScale = 0.97f) },
        exitTransition = { fadeOut(motion.snappy()) },
        popEnterTransition = { fadeIn(motion.gentle()) + scaleIn(motion.gentle(), initialScale = 0.97f) },
        // Also drives the predictive-back preview: the screen shrinks a touch as it fades.
        popExitTransition = { fadeOut(motion.snappy()) + scaleOut(motion.gentle(), targetScale = 0.97f) },
    ) {
        composable<Routes.Home> {
            HomeScreen(
                contentPadding = contentPadding,
                onQuickAction = { showMessage(it.comingSoon) },
                onRecipeClick = { showMessage("Recipe pages arrive in Phase 1.") },
            )
        }
        composable<Routes.Cookbook> {
            CookbookScreen(
                contentPadding = contentPadding,
                onCategoryClick = { showMessage("${it.name} opens in Phase 1.") },
            )
        }
        composable<Routes.Journal> {
            JournalScreen(contentPadding = contentPadding)
        }
        composable<Routes.Settings> {
            SettingsRoute(
                contentPadding = contentPadding,
                onOpenDesignSystem = { navController.navigate(Routes.DesignSystem) },
                showMessage = showMessage,
            )
        }
        composable<Routes.DesignSystem>(
            enterTransition = { slideIntoContainer(SlideDirection.Start, motion.hero()) },
            exitTransition = { fadeOut(motion.snappy()) },
            popEnterTransition = { fadeIn(motion.gentle()) },
            popExitTransition = { slideOutOfContainer(SlideDirection.End, motion.hero()) },
        ) {
            DesignSystemCatalog(onBack = { navController.popBackStack() })
        }
    }
}
