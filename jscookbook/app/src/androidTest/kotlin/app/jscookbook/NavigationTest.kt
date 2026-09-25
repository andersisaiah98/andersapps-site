package app.jscookbook

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a connected phone: ./gradlew connectedDebugAndroidTest */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBarSwitchesBetweenTheFourScreens() {
        compose.onNodeWithTag("screen:home").assertIsDisplayed()
        compose.onNodeWithTag("nav:home").assertIsSelected()

        listOf("cookbook", "journal", "settings", "home").forEach { tab ->
            compose.onNodeWithTag("nav:$tab").performClick()
            compose.onNodeWithTag("screen:$tab").assertIsDisplayed()
            compose.onNodeWithTag("nav:$tab").assertIsSelected()
        }
    }

    @Test
    fun backFromAnotherTabReturnsHome() {
        compose.onNodeWithTag("nav:journal").performClick()
        compose.onNodeWithTag("screen:journal").assertIsDisplayed()

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }

        compose.onNodeWithTag("screen:home").assertIsDisplayed()
        compose.onNodeWithTag("nav:home").assertIsSelected()
    }

    @Test
    fun addButtonOpensSheetWithThreeChoices() {
        compose.onNodeWithContentDescription("Add").performClick()

        compose.onNodeWithTag("sheet:add").assertIsDisplayed()
        compose.onNodeWithText("New recipe").assertIsDisplayed()
        compose.onNodeWithText("Snap a photo").assertIsDisplayed()
        compose.onNodeWithText("New category").assertIsDisplayed()
    }

    @Test
    fun tappingVersionSevenTimesOpensDesignSystem() {
        compose.onNodeWithTag("nav:settings").performClick()
        // The floating bar can cover the row on short screens, so click via semantics, not a touch.
        val version = compose.onNodeWithTag("settings:version").performScrollTo()
        repeat(7) { version.performSemanticsAction(SemanticsActions.OnClick) }

        compose.onNodeWithTag("screen:design_system").assertIsDisplayed()

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("screen:settings").assertIsDisplayed()
    }
}
