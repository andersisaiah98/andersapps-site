package app.jscookbook

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.ui.JsCookBookApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepSplashUntilThemeIsKnown()
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = uiState.themeMode.isDark(isSystemInDarkTheme())

            // Status and navigation bar icons follow the app's theme, not just the system's.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightNavScrim, DarkNavScrim) { darkTheme },
                )
                onDispose {}
            }

            JsTheme(darkTheme = darkTheme) {
                JsCookBookApp()
            }
        }
    }

    /** Holds the system splash for the few ms it takes to read the saved theme, so dark mode never flashes light. */
    private fun keepSplashUntilThemeIsKnown() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewModel.uiState.value.loading) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )
    }
}

// Same scrims androidx.activity uses by default; only visible with 3-button navigation.
private val LightNavScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkNavScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
