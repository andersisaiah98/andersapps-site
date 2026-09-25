package app.jscookbook.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalJsExtendedColors = staticCompositionLocalOf { LightExtendedColors }
private val DefaultShapes = JsShapes()
private val LocalJsShapes = staticCompositionLocalOf { DefaultShapes }
private val LocalJsMotion = staticCompositionLocalOf { JsMotion(reduced = false) }

/**
 * J's Cook Book theme: Benjamin Moore TerraCotta palette, Fraunces + DM Sans, 20/24/28 dp
 * corners, layered warm shadows and spring motion. Dynamic color is deliberately never used.
 *
 * @param reducedMotion defaults to the system "Remove animations" setting.
 */
@Composable
fun JsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = rememberSystemReducedMotion(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val typography = remember(extended.title) { JsTypography.withTitleColor(extended.title) }
    val motion = remember(reducedMotion) { JsMotion(reducedMotion) }

    CompositionLocalProvider(
        LocalJsExtendedColors provides extended,
        LocalJsShapes provides DefaultShapes,
        LocalJsMotion provides motion,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = JsMaterialShapes,
            content = content,
        )
    }
}

object JsTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val extendedColors: JsExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalJsExtendedColors.current

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: JsShapes
        @Composable @ReadOnlyComposable
        get() = LocalJsShapes.current

    val motion: JsMotion
        @Composable @ReadOnlyComposable
        get() = LocalJsMotion.current

    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalReducedMotion.current
}
