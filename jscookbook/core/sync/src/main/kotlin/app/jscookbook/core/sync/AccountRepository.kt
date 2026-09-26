package app.jscookbook.core.sync

import android.content.Intent
import app.jscookbook.core.data.sync.LocalSync
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed interface Account {
    /** No Supabase keys in this build: the app is local-only. */
    data object NotConfigured : Account
    data object Loading : Account
    data object SignedOut : Account
    data class SignedIn(val userId: String, val email: String?) : Account
}

@Singleton
class AccountRepository @Inject constructor(
    private val backend: Backend,
    private val local: LocalSync,
) {
    val googleSignInAvailable: Boolean get() = backend.config.hasGoogleSignIn

    /** For the native Google sign-in button (Credential Manager), when configured. */
    val composeAuth: ComposeAuth? get() = backend.client?.takeIf { googleSignInAvailable }?.composeAuth

    val account: Flow<Account> = backend.client?.let { client ->
        client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user
                    ?.let { Account.SignedIn(it.id, it.email) }
                    ?: Account.Loading
                SessionStatus.Initializing -> Account.Loading
                // Usually offline with an expired token: still signed in, it refreshes when back online.
                is SessionStatus.RefreshFailure -> client.auth.currentUserOrNull()
                    ?.let { Account.SignedIn(it.id, it.email) }
                    ?: Account.Loading
                is SessionStatus.NotAuthenticated -> Account.SignedOut
            }
        }.distinctUntilChanged()
    } ?: flowOf(Account.NotConfigured)

    /** Emails a sign-in link (and a 6-digit code) to [email]. */
    suspend fun sendMagicLink(email: String) {
        requireClient().auth.signInWith(OTP) {
            this.email = email.trim()
        }
    }

    /** Signs in with the 6-digit code from the same email, for when the link opens elsewhere. */
    suspend fun verifyCode(email: String, code: String) {
        requireClient().auth.verifyEmailOtp(type = OtpType.Email.EMAIL, email = email.trim(), token = code.filter(Char::isDigit))
    }

    /** Stops syncing and signs out. Recipes stay on this phone. */
    suspend fun signOut() {
        local.unlink()
        requireClient().auth.signOut()
    }

    /** Finishes a magic-link sign-in when app.jscookbook://login opens the app. */
    fun handleDeepLink(intent: Intent) {
        backend.client?.handleDeeplinks(intent)
    }

    private fun requireClient() = checkNotNull(backend.client) { "Sync isn't set up in this build" }
}
