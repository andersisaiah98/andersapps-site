package app.jscookbook.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.jscookbook.core.designsystem.modifier.paperBackground
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.ui.add.AddSheet
import app.jscookbook.ui.navigation.JsBottomBar
import app.jscookbook.ui.navigation.JsNavHost
import app.jscookbook.ui.navigation.TopLevelDestination
import app.jscookbook.ui.navigation.navigateToTopLevel
import app.jscookbook.ui.navigation.topLevelDestination
import kotlinx.coroutines.launch

/** The single-activity app shell: paper background, nav host, floating bar, add sheet, snackbars. */
@Composable
fun JsCookBookApp(modifier: Modifier = Modifier) {
    val colors = JsTheme.colors
    val extended = JsTheme.extendedColors
    val motion = JsTheme.motion
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var addSheetOpen by rememberSaveable { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = backStackEntry?.destination.topLevelDestination()
    // Keeps the bar's selection steady while it slides away for a non-tab screen.
    val lastTopLevel = remember { mutableStateOf(TopLevelDestination.Home) }
    LaunchedEffect(currentTopLevel) { currentTopLevel?.let { lastTopLevel.value = it } }

    val showMessage: (String) -> Unit = remember(scope, snackbarHostState) {
        { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
            Unit
        }
    }

    Box(modifier.fillMaxSize().paperBackground(colors.background, extended.paperGrain)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = colors.onBackground,
            // Screens pad for the status bar themselves so content can scroll under it.
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = JsTheme.shapes.control,
                        containerColor = colors.inverseSurface,
                        contentColor = colors.inverseOnSurface,
                        actionColor = colors.inversePrimary,
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = currentTopLevel != null,
                    enter = slideInVertically(motion.gentle()) { it } + fadeIn(motion.gentle()),
                    exit = slideOutVertically(motion.gentle()) { it } + fadeOut(motion.snappy()),
                ) {
                    JsBottomBar(
                        current = currentTopLevel ?: lastTopLevel.value,
                        onNavigate = navController::navigateToTopLevel,
                        addExpanded = addSheetOpen,
                        onAddClick = { addSheetOpen = true },
                    )
                }
            },
        ) { innerPadding ->
            JsNavHost(
                navController = navController,
                contentPadding = innerPadding,
                showMessage = showMessage,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // A soft fade under the status bar so scrolled content doesn't collide with the clock.
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Brush.verticalGradient(listOf(colors.background.copy(alpha = 0.94f), colors.background.copy(alpha = 0.6f)))),
        )

        if (addSheetOpen) {
            AddSheet(
                onDismiss = { addSheetOpen = false },
                onChoose = { choice -> showMessage(choice.comingSoon) },
            )
        }
    }
}
