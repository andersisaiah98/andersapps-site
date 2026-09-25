package app.jscookbook.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Material You dynamic color is never used: the wallpaper must not override the palette.

internal val LightColorScheme: ColorScheme = lightColorScheme(
    primary = BM.TerraCottaTile,
    onPrimary = Derived.White,
    primaryContainer = BM.GeorgetownPinkBeige,
    onPrimaryContainer = BM.Rustique,
    inversePrimary = Derived.TerraCottaLifted,
    secondary = BM.AudubonRusset,
    onSecondary = Derived.White,
    secondaryContainer = BM.MexicanTile,
    onSecondaryContainer = Derived.RustiqueDeep,
    tertiary = BM.VenetianPortico,
    onTertiary = Derived.RustiqueDeep,
    tertiaryContainer = BM.VenetianPortico,
    onTertiaryContainer = Derived.RustiqueDeep,
    background = BM.CloudWhite,
    onBackground = BM.IronMountain,
    surface = BM.CloudWhite,
    onSurface = BM.IronMountain,
    surfaceVariant = BM.EdgecombGray,
    onSurfaceVariant = BM.IronMountain,
    surfaceTint = Color.Transparent,
    inverseSurface = Derived.WarmCharcoal,
    inverseOnSurface = BM.DoveWing,
    error = Derived.ErrorLight,
    onError = Derived.OnErrorLight,
    errorContainer = Derived.ErrorContainerLight,
    onErrorContainer = Derived.OnErrorContainerLight,
    outline = BM.AntiquePewter,
    outlineVariant = BM.EdgecombGray,
    scrim = Color.Black,
    surfaceBright = BM.CloudWhite,
    surfaceDim = BM.EdgecombGray,
    surfaceContainerLowest = BM.CloudWhite,
    surfaceContainerLow = Derived.CloudDove,
    surfaceContainer = BM.DoveWing,
    surfaceContainerHigh = Derived.DoveEdgecomb,
    surfaceContainerHighest = BM.EdgecombGray,
)

internal val DarkColorScheme: ColorScheme = darkColorScheme(
    // Lifted terracotta so primary text and icons pass AA on charcoal. Filled brand surfaces
    // (buttons, the raised ＋) keep true Terra Cotta Tile via JsExtendedColors.brand.
    primary = Derived.TerraCottaLifted,
    onPrimary = Derived.RustiqueDeep,
    primaryContainer = BM.TerraCottaTile,
    onPrimaryContainer = Derived.White,
    inversePrimary = BM.TerraCottaTile,
    secondary = BM.MexicanTile,
    onSecondary = Derived.WarmCharcoal,
    secondaryContainer = Derived.RustiqueShadow,
    onSecondaryContainer = BM.GeorgetownPinkBeige,
    tertiary = BM.GeorgetownPinkBeige,
    onTertiary = Derived.RustiqueDeep,
    tertiaryContainer = Derived.RustiqueShadow,
    onTertiaryContainer = BM.GeorgetownPinkBeige,
    background = Derived.WarmCharcoal,
    onBackground = BM.DoveWing,
    surface = Derived.WarmCharcoal,
    onSurface = BM.DoveWing,
    surfaceVariant = Derived.CharcoalHighest,
    onSurfaceVariant = BM.VenetianPortico,
    surfaceTint = Color.Transparent,
    inverseSurface = BM.DoveWing,
    inverseOnSurface = Derived.WarmCharcoal,
    error = Derived.ErrorDark,
    onError = Derived.OnErrorDark,
    errorContainer = Derived.ErrorContainerDark,
    onErrorContainer = Derived.OnErrorContainerDark,
    outline = BM.AntiquePewter,
    outlineVariant = Derived.CharcoalOutline,
    scrim = Color.Black,
    surfaceBright = Derived.CharcoalHighest,
    surfaceDim = Derived.WarmCharcoal,
    surfaceContainerLowest = Derived.CharcoalLowest,
    surfaceContainerLow = Derived.CharcoalLow,
    surfaceContainer = Derived.CharcoalContainer,
    surfaceContainerHigh = Derived.CharcoalHigh,
    surfaceContainerHighest = Derived.CharcoalHighest,
)

/** Roles Material's ColorScheme doesn't have. Read through [JsTheme.extendedColors]. */
@Immutable
data class JsExtendedColors(
    /** True Terra Cotta Tile in both modes: filled buttons, the raised ＋, brand moments. */
    val brand: Color,
    val onBrand: Color,
    /** Terracotta for text and icons on the background (passes AA in both modes). */
    val brandText: Color,
    /** Fraunces display, headline and title text. */
    val title: Color,
    /** Large text, icons and outlines only. Never body copy. */
    val muted: Color,
    val navBar: Color,
    val navIndicator: Color,
    val navSelected: Color,
    val navUnselected: Color,
    /** Tint for the layered shadows. */
    val shadow: Color,
    val paperGrain: Color,
    /** Drop-target and focus glow. */
    val glow: Color,
    val isDark: Boolean,
)

internal val LightExtendedColors = JsExtendedColors(
    brand = BM.TerraCottaTile,
    onBrand = Derived.White,
    brandText = BM.TerraCottaTile,
    title = BM.Rustique,
    muted = BM.AntiquePewter,
    navBar = BM.DoveWing,
    navIndicator = BM.GeorgetownPinkBeige,
    navSelected = BM.Rustique,
    navUnselected = BM.IronMountain,
    shadow = BM.Rustique,
    paperGrain = BM.IronMountain,
    glow = BM.MexicanTile,
    isDark = false,
)

internal val DarkExtendedColors = JsExtendedColors(
    brand = BM.TerraCottaTile,
    onBrand = Derived.White,
    brandText = Derived.TerraCottaLifted,
    title = BM.GeorgetownPinkBeige,
    muted = BM.AntiquePewter,
    navBar = Derived.CharcoalHigh,
    navIndicator = Derived.RustiqueShadow,
    navSelected = BM.GeorgetownPinkBeige,
    navUnselected = BM.VenetianPortico,
    shadow = Color.Black,
    paperGrain = BM.DoveWing,
    glow = BM.MexicanTile,
    isDark = true,
)
