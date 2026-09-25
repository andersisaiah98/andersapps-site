package app.jscookbook.core.designsystem.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.jscookbook.core.designsystem.theme.BM
import app.jscookbook.core.designsystem.theme.Derived

/**
 * A colored card surface with text colors that pass AA (4.5:1) for body text and an ink for
 * illustrations. Use these for quick actions and categories.
 */
@Immutable
data class CardPalette(val key: String, val name: String, val container: Color, val content: Color, val ink: Color)

object CardPalettes {
    val TerraCotta = CardPalette("terracotta", "Terra Cotta Tile", BM.TerraCottaTile, Derived.White, BM.GeorgetownPinkBeige)
    val MexicanTile = CardPalette("mexican_tile", "Mexican Tile", BM.MexicanTile, Derived.RustiqueDeep, Derived.RustiqueDeep)
    val PinkBeige = CardPalette("pink_beige", "Georgetown Pink Beige", BM.GeorgetownPinkBeige, BM.Rustique, BM.TerraCottaTile)
    val Rustique = CardPalette("rustique", "Rustique", BM.Rustique, BM.GeorgetownPinkBeige, BM.MexicanTile)
    val Cinnamon = CardPalette("cinnamon", "Cinnamon", BM.Cinnamon, BM.DoveWing, BM.GeorgetownPinkBeige)
    val Edgecomb = CardPalette("edgecomb", "Edgecomb Gray", BM.EdgecombGray, BM.IronMountain, BM.Cinnamon)
    val Portico = CardPalette("portico", "Venetian Portico", BM.VenetianPortico, Derived.RustiqueDeep, BM.Rustique)

    val all = listOf(TerraCotta, MexicanTile, PinkBeige, Rustique, Cinnamon, Edgecomb, Portico)

    /** The palette stored under [key] (category colors), falling back to Terra Cotta. */
    fun fromKey(key: String): CardPalette = all.firstOrNull { it.key == key } ?: TerraCotta
}
