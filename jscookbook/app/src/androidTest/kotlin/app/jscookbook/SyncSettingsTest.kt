package app.jscookbook

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.sync.Account
import app.jscookbook.ui.settings.SyncSettings
import app.jscookbook.ui.settings.SyncUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The account / household / sync part of Settings, driven with fake state. */
@RunWith(AndroidJUnit4::class)
class SyncSettingsTest {

    @get:Rule
    val compose = createComposeRule()

    private var joinedWith: String? = null
    private var state by mutableStateOf(SyncUiState(account = Account.SignedOut))

    private fun show() = compose.setContent {
        JsTheme(darkTheme = false) {
            Column {
                SyncSettings(
                    state = state,
                    onGoogle = null,
                    onSendLink = { state = state.copy(linkSentTo = it) },
                    onVerifyCode = {},
                    onCancelEmail = {},
                    onSignOut = {},
                    onStartSharing = {},
                    onCreateInvite = { state = state.copy(inviteCode = "ABCDE-FGHJK") },
                    onDismissInvite = { state = state.copy(inviteCode = null) },
                    onJoin = { joinedWith = it },
                    onSyncNow = {},
                )
            }
        }
    }

    @Test
    fun emailSignInAsksForTheCodeAfterSending() {
        show()
        compose.onNodeWithTag("settings:email").performClick()
        compose.onNodeWithTag("signin:email").performTextInput("julia@example.com")
        compose.onNodeWithText("Send link").performClick()
        compose.onNodeWithTag("signin:code").assertIsDisplayed()
    }

    @Test
    fun signedInWithoutHouseholdCanJoinWithACode() {
        state = SyncUiState(account = Account.SignedIn("u1", "julia@example.com"))
        show()
        compose.onNodeWithText("julia@example.com").assertIsDisplayed()
        compose.onNodeWithTag("settings:join").performClick()
        compose.onNodeWithTag("join:code").performTextInput("abcde-fghjk")
        compose.onNodeWithText("Join").performClick()
        assertEquals("ABCDE-FGHJK", joinedWith)
    }

    @Test
    fun householdShowsInviteCodeAndSyncStatus() {
        state = SyncUiState(account = Account.SignedIn("u1", null), householdId = "cb", memberCount = 2, lastSyncedAt = System.currentTimeMillis())
        show()
        compose.onNodeWithText("2 people share this cook book").assertIsDisplayed()
        compose.onNodeWithText("Synced").assertIsDisplayed()
        compose.onNodeWithTag("settings:invite").performClick()
        compose.onNodeWithTag("invite:code").assertIsDisplayed()
    }

    @Test
    fun withoutKeysTheAppSaysItIsLocalOnly() {
        state = SyncUiState(account = Account.NotConfigured)
        show()
        compose.onNodeWithText("Sync isn't set up").assertIsDisplayed()
        compose.onNodeWithText("This phone only").assertIsDisplayed()
    }
}
