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
data class CardPalette(val name: String, val container: Color, val content: Color, val ink: Color)

object CardPalettes {
    val TerraCotta = CardPalette("Terra Cotta Tile", BM.TerraCottaTile, Derived.White, BM.GeorgetownPinkBeige)
    val MexicanTile = CardPalette("Mexican Tile", BM.MexicanTile, Derived.RustiqueDeep, Derived.RustiqueDeep)
    val PinkBeige = CardPalette("Georgetown Pink Beige", BM.GeorgetownPinkBeige, BM.Rustique, BM.TerraCottaTile)
    val Rustique = CardPalette("Rustique", BM.Rustique, BM.GeorgetownPinkBeige, BM.MexicanTile)
    val Cinnamon = CardPalette("Cinnamon", BM.Cinnamon, BM.DoveWing, BM.GeorgetownPinkBeige)
    val Edgecomb = CardPalette("Edgecomb Gray", BM.EdgecombGray, BM.IronMountain, BM.Cinnamon)
    val Portico = CardPalette("Venetian Portico", BM.VenetianPortico, Derived.RustiqueDeep, BM.Rustique)

    val all = listOf(TerraCotta, MexicanTile, PinkBeige, Rustique, Cinnamon, Edgecomb, Portico)
}
