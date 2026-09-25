package app.jscookbook.core.model

/** One line of an ingredient list as typed or pasted. */
sealed interface ParsedIngredientLine {
    /** "For the sauce:" */
    data class Section(val title: String) : ParsedIngredientLine

    data class Item(
        val quantity: String?,
        val unit: String?,
        val name: String,
        val note: String?,
    ) : ParsedIngredientLine
}

/**
 * Splits free-text ingredient lines into quantity, unit, name and note:
 *
 * - "2 cups flour, sifted" → 2 · cup · flour · sifted
 * - "1½ tbsp olive oil" → 1 1/2 · tbsp · olive oil
 * - "1 (14 oz) can tomatoes" → 1 · can · tomatoes · 14 oz
 * - "2-3 cloves garlic (minced)" → 2-3 · clove · garlic · minced
 * - "Salt to taste" → salt · to taste
 * - "For the sauce:" → a section heading
 *
 * Units are stored in a canonical form (tbsp, tsp, cup, g…) so later features like scaling
 * can rely on them. Anything it doesn't recognize stays in the name, so nothing is lost.
 */
object IngredientParser {

    fun parseLines(text: String): List<ParsedIngredientLine> =
        text.lines().map { it.trim().trimStart('-', '•', '*', '·').trim() }.filter { it.isNotEmpty() }.map(::parse)

    fun parse(line: String): ParsedIngredientLine {
        val trimmed = normalizeFractions(line.trim().replace(Regex("\\s+"), " "))
        if (trimmed.endsWith(":") && trimmed.length > 1) {
            return ParsedIngredientLine.Section(trimmed.dropLast(1).trim())
        }

        var rest = trimmed
        val quantity = QuantityPattern.find(rest)?.takeIf { it.range.first == 0 }?.let { match ->
            rest = rest.substring(match.range.last + 1).trim()
            normalizeQuantity(match.value)
        }

        // "1 (14 oz) can tomatoes": a size in brackets right after the number.
        var sizeNote: String? = null
        if (quantity != null && rest.startsWith("(")) {
            val close = rest.indexOf(')')
            if (close > 0) {
                sizeNote = rest.substring(1, close).trim()
                rest = rest.substring(close + 1).trim()
            }
        }

        var unit: String? = null
        var unitText: String? = null
        unitAt(rest)?.let { (canonical, length) ->
            unit = canonical
            unitText = rest.substring(0, length)
            rest = rest.substring(length).trim()
            if (rest.startsWith("of ", ignoreCase = true)) rest = rest.substring(3).trim()
        }

        var name = rest
        val notes = mutableListOf<String>()
        sizeNote?.let(notes::add)

        // Trailing "(optional)" or "(minced)".
        TrailingParens.find(name)?.let { match ->
            notes += match.groupValues[1].trim()
            name = name.substring(0, match.range.first).trim()
        }
        // "flour, sifted"
        val comma = name.indexOf(',')
        if (comma > 0) {
            notes.add(0, name.substring(comma + 1).trim())
            name = name.substring(0, comma).trim()
        }
        // "salt to taste"
        ToTaste.find(name)?.let { match ->
            notes += "to taste"
            name = name.substring(0, match.range.first).trim()
        }

        if (name.isEmpty()) {
            // Only a number and a unit ("2 cups"), or nothing parseable: keep the words as the name.
            name = unitText ?: trimmed
            unit = null
        }
        return ParsedIngredientLine.Item(
            quantity = quantity,
            unit = unit,
            name = name,
            note = notes.filter { it.isNotEmpty() }.joinToString(", ").ifEmpty { null },
        )
    }

    /** "1 1/2 cups flour, sifted" (plural unit when the quantity isn't 1). */
    fun format(quantity: String?, unit: String?, name: String, note: String?): String = buildString {
        if (!quantity.isNullOrBlank()) append(quantity).append(' ')
        if (!unit.isNullOrBlank()) append(displayUnit(unit, quantity)).append(' ')
        append(name)
        if (!note.isNullOrBlank()) append(", ").append(note)
    }.trim()

    fun displayUnit(unit: String, quantity: String?): String {
        val plural = quantity != null && quantity.trim() != "1" && quantity.trim() != "1/2" &&
            quantity.trim() != "1/4" && quantity.trim() != "1/3" && quantity.trim() != "3/4" && quantity.trim() != "2/3"
        return if (plural && unit in CountUnits) pluralize(unit) else unit
    }

    private fun pluralize(unit: String) = when {
        unit.endsWith("ch") || unit.endsWith("sh") -> unit + "es"
        else -> unit + "s"
    }

    // --- Quantities ---

    private val Fractions = mapOf(
        '½' to "1/2", '⅓' to "1/3", '⅔' to "2/3", '¼' to "1/4", '¾' to "3/4",
        '⅕' to "1/5", '⅖' to "2/5", '⅗' to "3/5", '⅘' to "4/5", '⅙' to "1/6",
        '⅚' to "5/6", '⅛' to "1/8", '⅜' to "3/8", '⅝' to "5/8", '⅞' to "7/8",
    )

    internal fun normalizeFractions(text: String): String {
        val out = StringBuilder()
        text.forEachIndexed { i, c ->
            val fraction = Fractions[c]
            if (fraction != null) {
                if (i > 0 && text[i - 1].isDigit()) out.append(' ')
                out.append(fraction)
            } else {
                out.append(c)
            }
        }
        return out.toString().replace('⁄', '/')
    }

    private const val Number = "(?:\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[.,]\\d+)?)"
    private val QuantityPattern = Regex("$Number(?:\\s*(?:-|–|—|to)\\s*$Number)?(?=\\s|$|[a-zA-Z(])")

    private fun normalizeQuantity(raw: String): String =
        raw.replace(Regex("\\s*(?:–|—|-)\\s*"), "-").replace(Regex("\\s+to\\s+"), "-").replace(',', '.').trim()

    // --- Units ---

    private val TrailingParens = Regex("\\(([^()]*)\\)\\s*$")
    private val ToTaste = Regex("\\s+to taste$", RegexOption.IGNORE_CASE)

    /** Count-style units that read as words and take a plural. */
    private val CountUnits = setOf(
        "cup", "clove", "can", "pinch", "dash", "stick", "slice", "bunch", "sprig", "handful",
        "piece", "package", "head", "jar", "bottle", "bag", "box",
    )

    /** Case-sensitive single letters: T is a tablespoon, t a teaspoon. */
    private val CaseSensitive = mapOf("T" to "tbsp", "Tb" to "tbsp", "t" to "tsp", "c" to "cup", "C" to "cup")

    private val Units: Map<String, String> = buildMap {
        fun add(canonical: String, vararg spellings: String) = spellings.forEach { put(it, canonical) }
        add("cup", "cup", "cups")
        add("tbsp", "tbsp", "tbsps", "tbs", "tblsp", "tablespoon", "tablespoons")
        add("tsp", "tsp", "tsps", "teaspoon", "teaspoons")
        add("fl oz", "fl oz", "fl. oz", "fluid ounce", "fluid ounces")
        add("oz", "oz", "ounce", "ounces")
        add("lb", "lb", "lbs", "pound", "pounds")
        add("g", "g", "gr", "gram", "grams", "gramme", "grammes")
        add("kg", "kg", "kilo", "kilos", "kilogram", "kilograms")
        add("mg", "mg", "milligram", "milligrams")
        add("ml", "ml", "mL", "milliliter", "milliliters", "millilitre", "millilitres")
        add("l", "l", "L", "liter", "liters", "litre", "litres")
        add("qt", "qt", "qts", "quart", "quarts")
        add("pt", "pt", "pts", "pint", "pints")
        add("gal", "gal", "gallon", "gallons")
        add("clove", "clove", "cloves")
        add("can", "can", "cans", "tin", "tins")
        add("pinch", "pinch", "pinches")
        add("dash", "dash", "dashes")
        add("stick", "stick", "sticks")
        add("slice", "slice", "slices")
        add("bunch", "bunch", "bunches")
        add("sprig", "sprig", "sprigs")
        add("handful", "handful", "handfuls")
        add("piece", "piece", "pieces", "pc", "pcs")
        add("package", "package", "packages", "pkg", "pkgs", "packet", "packets")
        add("head", "head", "heads")
        add("jar", "jar", "jars")
        add("bottle", "bottle", "bottles")
        add("bag", "bag", "bags")
        add("box", "box", "boxes")
    }

    /**
     * The canonical unit at the start of [text] and how many characters it spans. Tokens of one or
     * two letters must match exactly ("T" vs "t", "L", "mL"); longer ones ignore case.
     */
    private fun unitAt(text: String): Pair<String, Int>? {
        val words = text.split(' ')
        for (count in minOf(2, words.size) downTo 1) {
            val candidate = words.take(count).joinToString(" ")
            val bare = candidate.trimEnd('.')
            val canonical = if (bare.length <= 2) CaseSensitive[bare] ?: Units[bare] else Units[bare.lowercase()]
            if (canonical != null) return canonical to candidate.length
        }
        return null
    }
}
