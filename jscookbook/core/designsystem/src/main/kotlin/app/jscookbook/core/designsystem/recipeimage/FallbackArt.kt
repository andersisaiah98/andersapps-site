package app.jscookbook.core.designsystem.recipeimage

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Bowl
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Cookie
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Cup
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Cupcake
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Egg
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Glass
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Jar
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Loaf
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Plate
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Pot
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Skillet
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration.Whisk
import app.jscookbook.core.designsystem.theme.BM
import app.jscookbook.core.designsystem.theme.Derived
import app.jscookbook.core.model.RecipeType

enum class FoodIllustration(val label: String) {
    Bowl("Bowl"),
    Plate("Plate"),
    Skillet("Skillet"),
    Whisk("Whisk"),
    Cup("Cup"),
    Cookie("Cookie"),
    Pot("Pot"),
    Cupcake("Cupcake"),
    Glass("Glass"),
    Jar("Jar"),
    Loaf("Loaf"),
    Egg("Egg"),
    ;

    companion object {
        /** The illustration stored under [key] (a category icon), falling back to the bowl. */
        fun fromKey(key: String): FoodIllustration = entries.firstOrNull { it.name == key } ?: Bowl
    }
}

/** A tile background and the ink its illustration and initial are drawn in. */
@Immutable
data class FallbackTint(val name: String, val container: Color, val ink: Color)

object FallbackTints {
    /**
     * All palette colors. Every ink clears 3:1 on its container because the initial is always
     * large text. The order is part of the art's identity: append, never reorder.
     */
    val all: List<FallbackTint> = listOf(
        FallbackTint("Terra Cotta Tile", BM.TerraCottaTile, BM.GeorgetownPinkBeige),
        FallbackTint("Audubon Russet", BM.AudubonRusset, BM.CloudWhite),
        FallbackTint("Mexican Tile", BM.MexicanTile, Derived.RustiqueDeep),
        FallbackTint("Venetian Portico", BM.VenetianPortico, BM.Rustique),
        FallbackTint("Georgetown Pink Beige", BM.GeorgetownPinkBeige, BM.TerraCottaTile),
        FallbackTint("Rustique", BM.Rustique, BM.GeorgetownPinkBeige),
        FallbackTint("Cinnamon", BM.Cinnamon, BM.DoveWing),
        FallbackTint("Antique Pewter", BM.AntiquePewter, BM.CloudWhite),
        FallbackTint("Edgecomb Gray", BM.EdgecombGray, BM.IronMountain),
        FallbackTint("Iron Mountain", BM.IronMountain, BM.GeorgetownPinkBeige),
    )
}

/** Everything needed to draw a recipe's fallback tile. */
@Immutable
data class FallbackArt(
    val illustration: FoodIllustration,
    val tint: FallbackTint,
    /** The title's first letter or digit, uppercased; empty if the title has none. */
    val initial: String,
    /** A small tilt for character, in degrees (−8..8). */
    val tiltDegrees: Float,
)

/**
 * Picks fallback art deterministically, so a recipe looks the same on both phones and after
 * every restart. The hash is FNV-1a (32-bit) over the normalized title's UTF-8 bytes; don't
 * change it or the tint lists' order, or every recipe changes color.
 *
 * The illustration comes from the first match of: a food word in the title ("…soup"), a food word
 * in a category name ("Baking"), the recipe type, or all illustrations.
 */
object FallbackArtSelector {

    fun select(title: String, type: RecipeType?, categoryNames: List<String> = emptyList()): FallbackArt {
        val hash = fnv1a32(normalize(title))
        val tints = FallbackTints.all
        val candidates = illustrationsFor(title, type, categoryNames)
        return FallbackArt(
            illustration = candidates[(hash ushr 16).mod(candidates.size)],
            tint = tints[hash.mod(tints.size)],
            initial = initialOf(title),
            tiltDegrees = ((hash ushr 8) and 0xFF) / 255f * 16f - 8f,
        )
    }

    /** Candidate illustrations in priority order: title words, then category words, then type. */
    fun illustrationsFor(title: String, type: RecipeType?, categoryNames: List<String>): List<FoodIllustration> =
        keywordMatch(words(title))
            ?: keywordMatch(categoryNames.flatMap(::words))
            ?: type?.let(TypeIllustrations::getValue)
            ?: FoodIllustration.entries

    internal fun normalize(title: String): String =
        title.trim().lowercase().split(Whitespace).filter { it.isNotEmpty() }.joinToString(" ")

    internal fun fnv1a32(text: String): Int {
        var hash = 0x811C9DC5.toInt()
        for (byte in text.encodeToByteArray()) {
            hash = hash xor (byte.toInt() and 0xFF)
            hash *= 0x01000193
        }
        return hash
    }

    internal fun initialOf(title: String): String =
        title.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: ""

    private val Whitespace = Regex("\\s+")
    private val NonLetters = Regex("[^\\p{L}]+")

    private fun words(text: String): List<String> =
        text.lowercase().split(NonLetters).filter { it.isNotEmpty() }

    private fun keywordMatch(words: List<String>): List<FoodIllustration>? {
        if (words.isEmpty()) return null
        return KeywordRules.firstOrNull { rule -> words.any { word -> rule.keywords.any { word.matches(it) } } }
            ?.illustrations
    }

    /** A keyword matches the whole word or its plural; a trailing `*` makes it a prefix. */
    private fun String.matches(keyword: String): Boolean = when {
        keyword.endsWith('*') -> startsWith(keyword.dropLast(1))
        this == keyword || this == keyword + "s" || this == keyword + "es" -> true
        else -> keyword.endsWith('y') && this == keyword.dropLast(1) + "ies"
    }

    private class Rule(val keywords: List<String>, val illustrations: List<FoodIllustration>)

    private val KeywordRules = listOf(
        Rule(listOf("soup", "stew", "chili", "chowder", "broth", "bisque", "curry"), listOf(Pot, Bowl)),
        Rule(listOf("salad", "slaw", "grain", "poke", "noodle", "ramen", "pasta", "risotto", "oat", "porridge"), listOf(Bowl)),
        Rule(listOf("bread", "loaf", "loaves", "bak*", "muffin", "scone", "focaccia", "biscuit", "roll"), listOf(Loaf)),
        Rule(listOf("cookie", "brownie", "blondie"), listOf(Cookie)),
        Rule(listOf("cake", "cupcake", "dessert", "sweets", "treat", "pudding", "cheesecake"), listOf(Cupcake)),
        Rule(listOf("coffee", "tea", "latte", "cocoa", "chai", "espresso"), listOf(Cup)),
        Rule(listOf("drink", "cocktail", "mocktail", "smoothie", "juice", "lemonade", "soda", "shake"), listOf(Glass)),
        Rule(listOf("sauce", "dressing", "vinaigrette", "dip", "jam", "pickle", "salsa", "pesto", "gravy", "staple"), listOf(Jar)),
        Rule(listOf("egg", "omelet", "omelette", "frittata", "shakshuka", "quiche"), listOf(Egg, Skillet)),
        Rule(listOf("pancake", "waffle", "breakfast", "brunch", "hash", "skillet", "fried", "sear*", "grill*", "fajita"), listOf(Skillet)),
        Rule(listOf("whip*", "meringue", "custard", "batter", "frosting", "icing"), listOf(Whisk)),
    )

    private val TypeIllustrations: Map<RecipeType, List<FoodIllustration>> = mapOf(
        RecipeType.Breakfast to listOf(Skillet, Egg, Cup, Loaf),
        RecipeType.Meal to listOf(Plate, Pot, Bowl, Skillet),
        RecipeType.Snack to listOf(Cookie, Bowl, Loaf),
        RecipeType.Side to listOf(Bowl, Plate),
        RecipeType.Dessert to listOf(Cupcake, Cookie, Whisk),
        RecipeType.Drink to listOf(Glass, Cup),
        RecipeType.Sauce to listOf(Jar, Whisk, Pot),
    )
}
