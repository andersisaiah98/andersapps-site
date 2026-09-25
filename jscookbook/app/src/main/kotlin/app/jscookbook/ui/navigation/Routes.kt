package app.jscookbook.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import app.jscookbook.core.designsystem.icon.JsIcons
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/** Type-safe Navigation Compose routes. */
object Routes {
    @Serializable data object Home

    @Serializable data object Cookbook

    @Serializable data object Journal

    @Serializable data object Settings

    @Serializable data object DesignSystem

    @Serializable data class RecipeDetail(val recipeId: String, val origin: String)

    /** No [recipeId] means a new recipe; [categoryId] preselects a category; [camera] opens the camera first. */
    @Serializable data class RecipeEditor(
        val recipeId: String? = null,
        val categoryId: String? = null,
        val camera: Boolean = false,
    )

    @Serializable data class CategoryRecipes(val categoryId: String)

    @Serializable data class PhotoViewer(val recipeId: String, val index: Int)
}

/** The four bottom-bar destinations. The raised ＋ in the middle is an action, not a destination. */
enum class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val testTag: String,
) {
    Home(Routes.Home, "Home", JsIcons.Home, JsIcons.HomeActive, "home"),
    Cookbook(Routes.Cookbook, "Cookbook", JsIcons.Cookbook, JsIcons.CookbookActive, "cookbook"),
    Journal(Routes.Journal, "Journal", JsIcons.Journal, JsIcons.JournalActive, "journal"),
    Settings(Routes.Settings, "Settings", JsIcons.Settings, JsIcons.SettingsActive, "settings"),
    ;

    val routeClass: KClass<*> get() = route::class
}

fun NavDestination?.topLevelDestination(): TopLevelDestination? {
    val destination = this ?: return null
    return TopLevelDestination.entries.firstOrNull { top ->
        destination.hierarchy.any { it.hasRoute(top.routeClass) }
    }
}

/** Switches tabs the standard way: one copy of each tab, each tab keeps its own scroll state. */
fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
