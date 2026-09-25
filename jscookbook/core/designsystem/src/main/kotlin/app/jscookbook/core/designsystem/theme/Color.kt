package app.jscookbook.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Benjamin Moore paints from Amerie Creative's "TerraCotta" palette. These are the only
 * brand colors; anything else lives in [Derived] and says where it came from.
 */
object BM {
    /** OC-130. Background. */
    val CloudWhite = Color(0xFFF2F1E6)

    /** OC-18. Surface and cards. */
    val DoveWing = Color(0xFFE9E6DB)

    /** HC-173. Surface variant and dividers. */
    val EdgecombGray = Color(0xFFD9D3C4)

    /** 2090-30. Primary. 5.1:1 on Cloud White; white on it is 5.8:1. */
    val TerraCottaTile = Color(0xFFA14C3E)

    /** HC-56. Primary container. */
    val GeorgetownPinkBeige = Color(0xFFDFC1AB)

    /** HC-51. Secondary. 3.7:1 on Cloud White, so large text, icons and outlines only. */
    val AudubonRusset = Color(0xFFAE6B55)

    /** 1194. Secondary container. */
    val MexicanTile = Color(0xFFC18872)

    /** AF-185. Tertiary / warm neutral. */
    val VenetianPortico = Color(0xFFC4A996)

    /** AF-275. Deep accent; headline color in light mode (7.2:1 on Cloud White). */
    val Rustique = Color(0xFF7C3E2E)

    /** 2174-20. Accent 2. */
    val Cinnamon = Color(0xFF984F3B)

    /** 2134-30. Body text and icons. 6.5:1 on Cloud White. */
    val IronMountain = Color(0xFF575553)

    /** 1560. Muted text and outlines. 3.1:1 on Cloud White, so large text, icons and outlines only. */
    val AntiquePewter = Color(0xFF8B8A7B)
}

/**
 * Colors that are not Benjamin Moore paints. Each is derived from a BM hue (mostly for dark
 * mode) or is a functional color the palette doesn't cover.
 */
object Derived {
    /** Derived from Iron Mountain: the warm charcoal dark-mode background. */
    val WarmCharcoal = Color(0xFF1E1B19)

    /** Derived from Warm Charcoal: dark surface container steps, lowest to highest. */
    val CharcoalLowest = Color(0xFF181513)
    val CharcoalLow = Color(0xFF231F1D)
    val CharcoalContainer = Color(0xFF2A2522)
    val CharcoalHigh = Color(0xFF322C28)
    val CharcoalHighest = Color(0xFF3B3430)

    /** Derived from Warm Charcoal: dark-mode dividers. */
    val CharcoalOutline = Color(0xFF4A423C)

    /** Derived from Terra Cotta Tile, lifted so terracotta text reads on charcoal (5.7:1). */
    val TerraCottaLifted = Color(0xFFD0806C)

    /** Derived from Rustique, pushed very dark: text on Mexican Tile and lifted terracotta. */
    val RustiqueDeep = Color(0xFF3B1D15)

    /** Derived from Rustique toward charcoal: soft brand container in dark mode. */
    val RustiqueShadow = Color(0xFF4A2E25)

    /** Derived: midpoint of Cloud White and Dove Wing. */
    val CloudDove = Color(0xFFEEEDE1)

    /** Derived: midpoint of Dove Wing and Edgecomb Gray. */
    val DoveEdgecomb = Color(0xFFE1DDD0)

    /** Functional: text on Terra Cotta Tile. */
    val White = Color(0xFFFFFFFF)

    /** Functional: the palette has no red, so errors use Material's baseline error tones. */
    val ErrorLight = Color(0xFFB3261E)
    val OnErrorLight = Color(0xFFFFFFFF)
    val ErrorContainerLight = Color(0xFFF9DEDC)
    val OnErrorContainerLight = Color(0xFF410E0B)
    val ErrorDark = Color(0xFFF2B8B5)
    val OnErrorDark = Color(0xFF601410)
    val ErrorContainerDark = Color(0xFF8C1D18)
    val OnErrorContainerDark = Color(0xFFF9DEDC)
}

/** One entry in the design-system color list. */
@Immutable
data class Swatch(
    val name: String,
    val code: String?,
    val color: Color,
    val role: String,
) {
    val hex: String get() = "#%06X".format(color.toArgb() and 0xFFFFFF)
    val isDerived: Boolean get() = code == null
}

object Swatches {
    val benjaminMoore: List<Swatch> = listOf(
        Swatch("Cloud White", "OC-130", BM.CloudWhite, "Background"),
        Swatch("Dove Wing", "OC-18", BM.DoveWing, "Surface / cards"),
        Swatch("Edgecomb Gray", "HC-173", BM.EdgecombGray, "Surface variant / dividers"),
        Swatch("Terra Cotta Tile", "2090-30", BM.TerraCottaTile, "Primary"),
        Swatch("Georgetown Pink Beige", "HC-56", BM.GeorgetownPinkBeige, "Primary container"),
        Swatch("Audubon Russet", "HC-51", BM.AudubonRusset, "Secondary (large text, icons)"),
        Swatch("Mexican Tile", "1194", BM.MexicanTile, "Secondary container"),
        Swatch("Venetian Portico", "AF-185", BM.VenetianPortico, "Tertiary / warm neutral"),
        Swatch("Rustique", "AF-275", BM.Rustique, "Deep accent, headlines"),
        Swatch("Cinnamon", "2174-20", BM.Cinnamon, "Accent 2"),
        Swatch("Iron Mountain", "2134-30", BM.IronMountain, "Body text / icons"),
        Swatch("Antique Pewter", "1560", BM.AntiquePewter, "Muted / outlines (large only)"),
    )

    val derived: List<Swatch> = listOf(
        Swatch("Warm Charcoal", null, Derived.WarmCharcoal, "Dark background (from Iron Mountain)"),
        Swatch("Charcoal Low", null, Derived.CharcoalLow, "Dark surface, low"),
        Swatch("Charcoal Container", null, Derived.CharcoalContainer, "Dark cards"),
        Swatch("Charcoal High", null, Derived.CharcoalHigh, "Dark nav bar"),
        Swatch("Charcoal Highest", null, Derived.CharcoalHighest, "Dark surface, highest"),
        Swatch("Terra Cotta Lifted", null, Derived.TerraCottaLifted, "Dark primary text (from Terra Cotta Tile)"),
        Swatch("Rustique Deep", null, Derived.RustiqueDeep, "Text on tile colors (from Rustique)"),
        Swatch("Rustique Shadow", null, Derived.RustiqueShadow, "Dark brand container (from Rustique)"),
        Swatch("Cloud Dove", null, Derived.CloudDove, "Light surface, low"),
        Swatch("Dove Edgecomb", null, Derived.DoveEdgecomb, "Light surface, high"),
    )
}
