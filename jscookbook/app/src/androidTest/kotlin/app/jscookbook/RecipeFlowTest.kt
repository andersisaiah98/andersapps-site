package app.jscookbook

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Runs on a connected phone: ./gradlew connectedDebugAndroidTest. Leaves one test recipe behind. */
@RunWith(AndroidJUnit4::class)
class RecipeFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun createRecipeThenFindItInTheCookbook() {
        val title = "Test Soup ${UUID.randomUUID().toString().take(4)}"

        compose.onNodeWithContentDescription("Add").performClick()
        compose.onNodeWithText("New recipe").performClick()
        compose.onNodeWithTag("screen:editor").assertIsDisplayed()
        compose.onNodeWithTag("editor:title").performTextInput(title)
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("screen:recipe").assertIsDisplayed()

        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("nav:cookbook").performClick()
        compose.onNodeWithTag("screen:cookbook").performScrollToNode(hasText(title))
        compose.onNodeWithText(title).assertIsDisplayed()
    }
}
