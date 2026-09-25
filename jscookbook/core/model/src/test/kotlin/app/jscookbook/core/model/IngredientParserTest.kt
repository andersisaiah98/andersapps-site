package app.jscookbook.core.model

import app.jscookbook.core.model.ParsedIngredientLine.Item
import app.jscookbook.core.model.ParsedIngredientLine.Section
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientParserTest {

    private fun item(line: String) = IngredientParser.parse(line) as Item

    @Test
    fun quantityUnitNameAndNote() {
        assertEquals(Item("2", "cup", "flour", "sifted"), item("2 cups flour, sifted"))
        assertEquals(Item("1", "tbsp", "olive oil", null), item("1 tablespoon olive oil"))
        assertEquals(Item("200", "g", "butter", "softened"), item("200 g butter, softened"))
    }

    @Test
    fun fractionsMixedNumbersAndRanges() {
        assertEquals(Item("1 1/2", "tbsp", "olive oil", null), item("1½ tbsp olive oil"))
        assertEquals(Item("1/2", "tsp", "salt", null), item("½ tsp salt"))
        assertEquals(Item("1 1/2", "cup", "milk", null), item("1 1/2 cups milk"))
        assertEquals(Item("2-3", "clove", "garlic", "minced"), item("2-3 cloves garlic (minced)"))
        assertEquals(Item("2-3", "clove", "garlic", null), item("2 to 3 cloves garlic"))
        assertEquals(Item("1.5", "kg", "potatoes", null), item("1,5 kg potatoes"))
    }

    @Test
    fun singleLetterUnitsAreCaseSensitive() {
        assertEquals(Item("1", "tbsp", "sugar", null), item("1 T sugar"))
        assertEquals(Item("1", "tsp", "vanilla", null), item("1 t vanilla"))
        assertEquals(Item("2", "l", "water", null), item("2 L water"))
    }

    @Test
    fun sizeInBracketsBecomesTheNote() {
        assertEquals(Item("1", "can", "crushed tomatoes", "14 oz"), item("1 (14 oz) can crushed tomatoes"))
    }

    @Test
    fun countsWithoutUnitsKeepTheName() {
        assertEquals(Item("3", null, "large eggs", null), item("3 large eggs"))
        assertEquals(Item("1", null, "onion", "diced"), item("1 onion, diced"))
    }

    @Test
    fun noQuantityAndToTaste() {
        assertEquals(Item(null, null, "Salt", "to taste"), item("Salt to taste"))
        assertEquals(Item(null, null, "Fresh basil", "optional"), item("Fresh basil (optional)"))
    }

    @Test
    fun ofAfterUnitIsDropped() {
        assertEquals(Item(null, "pinch", "nutmeg", null), item("pinch of nutmeg"))
        assertEquals(Item("2", "cup", "chicken stock", null), item("2 cups of chicken stock"))
    }

    @Test
    fun unitAloneStaysAsName() {
        assertEquals(Item("2", null, "cups", null), item("2 cups"))
    }

    @Test
    fun sectionHeadings() {
        assertEquals(Section("For the sauce"), IngredientParser.parse("For the sauce:"))
    }

    @Test
    fun pastedListsSkipBulletsAndBlankLines() {
        val lines = IngredientParser.parseLines(
            """
            For the dough:
            - 2 cups flour
            • 1 tsp salt

            * 3/4 cup warm water
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                Section("For the dough"),
                Item("2", "cup", "flour", null),
                Item("1", "tsp", "salt", null),
                Item("3/4", "cup", "warm water", null),
            ),
            lines,
        )
    }

    @Test
    fun formatRoundTrips() {
        assertEquals("2 cups flour, sifted", IngredientParser.format("2", "cup", "flour", "sifted"))
        assertEquals("1 cup milk", IngredientParser.format("1", "cup", "milk", null))
        assertEquals("1/2 cup milk", IngredientParser.format("1/2", "cup", "milk", null))
        assertEquals("3 cloves garlic", IngredientParser.format("3", "clove", "garlic", null))
        assertEquals("2 tbsp butter", IngredientParser.format("2", "tbsp", "butter", null))
        assertEquals("Salt, to taste", IngredientParser.format(null, null, "Salt", "to taste"))
    }

    @Test
    fun durations() {
        assertEquals("45 min", formatMinutes(45))
        assertEquals("1 hr", formatMinutes(60))
        assertEquals("1 hr 15 min", formatMinutes(75))
        assertEquals("1:05", formatCountdown(65))
        assertEquals("1:02:03", formatCountdown(3723))
        assertEquals(null, totalMinutes(null, null))
        assertEquals(40, totalMinutes(10, 30))
    }
}
