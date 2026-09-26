package app.jscookbook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jscookbook.core.data.sync.LocalSync
import app.jscookbook.core.sync.Account
import app.jscookbook.core.sync.AccountRepository
import app.jscookbook.core.sync.HouseholdRepository
import app.jscookbook.core.sync.SyncActivity
import app.jscookbook.core.sync.SyncEngine
import app.jscookbook.core.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.compose.auth.ComposeAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val account: Account = Account.Loading,
    /** Null while this phone isn't in a household. */
    val householdId: String? = null,
    val memberCount: Int = 0,
    val activity: SyncActivity = SyncActivity.Idle,
    val lastSyncedAt: Long? = null,
    val pendingChanges: Int = 0,
    val googleSignInAvailable: Boolean = false,
    /** A request (sign-in, sharing, joining) is in flight. */
    val busy: Boolean = false,
    /** Set once the email link has been sent, so the screen can ask for the code. */
    val linkSentTo: String? = null,
    val inviteCode: String? = null,
)

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val accounts: AccountRepository,
    private val household: HouseholdRepository,
    private val scheduler: SyncScheduler,
    engine: SyncEngine,
    local: LocalSync,
) : ViewModel() {

    private val ui = MutableStateFlow(LocalUi())
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val composeAuth: ComposeAuth? = accounts.composeAuth

    val uiState: StateFlow<SyncUiState> = combine(
        accounts.account,
        household.observeLinked(),
        household.observeMemberCount(),
        combine(engine.activity, local.observeLastSyncedAt(), local.observePendingCount(), ::Triple),
        ui,
    ) { account, linked, members, (activity, lastSynced, pending), transient ->
        SyncUiState(
            account = account,
            householdId = linked,
            memberCount = members,
            activity = activity,
            lastSyncedAt = lastSynced?.toLongOrNull(),
            pendingChanges = pending,
            googleSignInAvailable = accounts.googleSignInAvailable,
            busy = transient.busy,
            linkSentTo = transient.linkSentTo,
            inviteCode = transient.inviteCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    fun sendLink(email: String) = run("Couldn't send the sign-in email") {
        accounts.sendMagicLink(email)
        ui.value = ui.value.copy(linkSentTo = email.trim())
    }

    fun verifyCode(code: String) = run("That code didn't work") {
        val email = checkNotNull(ui.value.linkSentTo)
        accounts.verifyCode(email, code)
        ui.value = ui.value.copy(linkSentTo = null)
    }

    fun cancelEmailSignIn() {
        ui.value = ui.value.copy(linkSentTo = null)
    }

    fun onGoogleResult(error: String?) {
        if (error != null) _messages.trySend("Google sign-in didn't finish: $error")
    }

    fun signOut() = run("Couldn't sign out") { accounts.signOut() }

    fun startSharing() = run("Couldn't start sharing") {
        household.startSharing()
        _messages.send("Sharing on. Invite the other phone next.")
    }

    fun createInvite() = run("Couldn't make an invite code") {
        ui.value = ui.value.copy(inviteCode = household.createInvite())
    }

    fun dismissInvite() {
        ui.value = ui.value.copy(inviteCode = null)
    }

    fun join(code: String) = run("Couldn't join") {
        household.join(code)
        _messages.send("Joined. Recipes are on their way.")
    }

    fun syncNow() {
        scheduler.syncNow()
    }

    private fun run(failure: String, block: suspend () -> Unit) {
        if (ui.value.busy) return
        viewModelScope.launch {
            ui.value = ui.value.copy(busy = true)
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("$failure. ${e.friendlyMessage()}")
            } finally {
                ui.value = ui.value.copy(busy = false)
            }
        }
    }

    private data class LocalUi(val busy: Boolean = false, val linkSentTo: String? = null, val inviteCode: String? = null)
}

/** Server errors carry a long description; the first line is the useful part. */
private fun Exception.friendlyMessage(): String =
    (message ?: "Check your connection and try again.").lineSequence().first().take(160)
