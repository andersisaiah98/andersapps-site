package app.jscookbook.ui.settings

import android.content.ClipData
import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.sync.Account
import app.jscookbook.core.sync.SyncActivity
import app.jscookbook.ui.common.LocalMessenger
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch

@Composable
fun SyncSettingsRoute(viewModel: SyncSettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { messenger.show(it) }
    }

    // Native Google sign-in (Credential Manager), only when this build has a Google client id.
    val startGoogle: (() -> Unit)? = viewModel.composeAuth?.let { composeAuth ->
        val google = composeAuth.rememberSignInWithGoogle(
            onResult = { result ->
                viewModel.onGoogleResult(
                    when (result) {
                        is NativeSignInResult.Success, NativeSignInResult.ClosedByUser -> null
                        is NativeSignInResult.NetworkError -> result.message
                        is NativeSignInResult.Error -> result.message
                    },
                )
            },
        )
        { google.startFlow() }
    }

    SyncSettings(
        state = state,
        onGoogle = startGoogle,
        onSendLink = viewModel::sendLink,
        onVerifyCode = viewModel::verifyCode,
        onCancelEmail = viewModel::cancelEmailSignIn,
        onSignOut = viewModel::signOut,
        onStartSharing = viewModel::startSharing,
        onCreateInvite = viewModel::createInvite,
        onDismissInvite = viewModel::dismissInvite,
        onJoin = viewModel::join,
        onSyncNow = viewModel::syncNow,
    )
}

@Composable
fun SyncSettings(
    state: SyncUiState,
    onGoogle: (() -> Unit)?,
    onSendLink: (String) -> Unit,
    onVerifyCode: (String) -> Unit,
    onCancelEmail: () -> Unit,
    onSignOut: () -> Unit,
    onStartSharing: () -> Unit,
    onCreateInvite: () -> Unit,
    onDismissInvite: () -> Unit,
    onJoin: (String) -> Unit,
    onSyncNow: () -> Unit,
) {
    var emailDialog by rememberSaveable { mutableStateOf(false) }
    var joinDialog by rememberSaveable { mutableStateOf(false) }
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }

    SettingsGroup("Account") {
        when (val account = state.account) {
            Account.NotConfigured -> SettingsRow(
                icon = JsIcons.Person,
                title = "Sync isn't set up",
                subtitle = "This build has no Supabase keys, so everything stays on this phone",
                onClick = null,
            )
            Account.Loading -> SettingsRow(icon = JsIcons.Person, title = "Account", subtitle = "Checking…", onClick = null)
            Account.SignedOut -> {
                if (onGoogle != null) {
                    SettingsRow(
                        icon = JsIcons.Person,
                        title = "Sign in with Google",
                        subtitle = "To share the cook book between your phones",
                        onClick = onGoogle,
                        modifier = Modifier.testTag("settings:google"),
                    )
                    RowDivider()
                }
                SettingsRow(
                    icon = JsIcons.Link,
                    title = "Sign in with email",
                    subtitle = "We'll email you a link and a code",
                    onClick = { emailDialog = true },
                    modifier = Modifier.testTag("settings:email"),
                )
            }
            is Account.SignedIn -> SettingsRow(
                icon = JsIcons.Person,
                title = account.email ?: "Signed in",
                subtitle = "Sign out",
                onClick = { confirmSignOut = true },
                modifier = Modifier.testTag("settings:account"),
            )
        }
    }

    if (state.account is Account.SignedIn) {
        SettingsGroup("Household") {
            if (state.householdId == null) {
                SettingsRow(
                    icon = JsIcons.People,
                    title = "Share this cook book",
                    subtitle = "Start here on the first phone",
                    onClick = onStartSharing.takeUnless { state.busy },
                    modifier = Modifier.testTag("settings:share"),
                )
                RowDivider()
                SettingsRow(
                    icon = JsIcons.Link,
                    title = "Join with a code",
                    subtitle = "On the second phone. Its recipes come along.",
                    onClick = { joinDialog = true }.takeUnless { state.busy },
                    modifier = Modifier.testTag("settings:join"),
                )
            } else {
                SettingsRow(
                    icon = JsIcons.People,
                    title = "Invite the other phone",
                    subtitle = when (state.memberCount) {
                        0, 1 -> "Only you so far"
                        else -> "${state.memberCount} people share this cook book"
                    },
                    onClick = onCreateInvite.takeUnless { state.busy },
                    modifier = Modifier.testTag("settings:invite"),
                )
            }
        }
    }

    SettingsGroup("Sync") {
        val (label, subtitle) = syncStatus(state)
        SettingsRow(
            icon = JsIcons.Cloud,
            title = "Sync",
            subtitle = subtitle,
            onClick = onSyncNow.takeIf { state.householdId != null && state.account is Account.SignedIn },
            showChevron = false,
            trailing = {
                StatusDot(
                    label = label,
                    color = when (state.activity) {
                        is SyncActivity.Failed -> JsTheme.colors.error
                        SyncActivity.Syncing -> JsTheme.extendedColors.brand
                        SyncActivity.Idle -> if (state.householdId != null) JsTheme.colors.tertiary else JsTheme.colors.outline
                    },
                )
            },
            modifier = Modifier.testTag("settings:sync"),
        )
    }

    if (emailDialog) {
        EmailSignInDialog(
            sentTo = state.linkSentTo,
            busy = state.busy,
            onSend = onSendLink,
            onVerify = onVerifyCode,
            onDismiss = {
                emailDialog = false
                onCancelEmail()
            },
        )
        // Close once signed in (by the code here or by the link in the email).
        LaunchedEffect(state.account) {
            if (state.account is Account.SignedIn) emailDialog = false
        }
    }

    if (joinDialog) {
        JoinDialog(
            busy = state.busy,
            onJoin = {
                onJoin(it)
                joinDialog = false
            },
            onDismiss = { joinDialog = false },
        )
    }

    state.inviteCode?.let { code -> InviteDialog(code = code, onDismiss = onDismissInvite) }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("Recipes stay on this phone. They stop syncing until you sign in again.") },
            confirmButton = {
                JsButton("Sign out", onClick = {
                    confirmSignOut = false
                    onSignOut()
                }, style = JsButtonStyle.Text)
            },
            dismissButton = { JsButton("Cancel", onClick = { confirmSignOut = false }, style = JsButtonStyle.Text) },
            shape = JsTheme.shapes.card,
            containerColor = JsTheme.colors.surfaceContainerHigh,
        )
    }
}

/** (short label for the dot, sentence for the row). */
private fun syncStatus(state: SyncUiState): Pair<String, String> = when {
    state.account is Account.NotConfigured -> "Local" to "This phone only"
    state.account !is Account.SignedIn -> "Local" to "Sign in to sync"
    state.householdId == null -> "Local" to "Share or join a cook book to sync"
    state.activity is SyncActivity.Syncing -> "Syncing" to "Syncing…"
    state.activity is SyncActivity.Failed ->
        "Retrying" to "Couldn't sync: ${(state.activity as SyncActivity.Failed).message}. Tap to try again."
    state.pendingChanges > 0 ->
        "Waiting" to "${state.pendingChanges} ${if (state.pendingChanges == 1) "change" else "changes"} waiting to upload"
    else -> "Synced" to (
        state.lastSyncedAt?.let {
            "Up to date · " + DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        } ?: "Waiting for the first sync"
        )
}

@Composable
private fun EmailSignInDialog(
    sentTo: String?,
    busy: Boolean,
    onSend: (String) -> Unit,
    onVerify: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    val validEmail = email.contains('@') && email.substringAfter('@').contains('.')
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sentTo == null) "Sign in with email" else "Check your email") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (sentTo == null) {
                    Text("We'll send a sign-in link. Open it on this phone.")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (validEmail && !busy) onSend(email) }),
                        modifier = Modifier.fillMaxWidth().testTag("signin:email"),
                    )
                } else {
                    Text("Tap the link we sent to $sentTo, or type the 6-digit code from the same email.")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        label = { Text("Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (code.length == 6 && !busy) onVerify(code) }),
                        modifier = Modifier.fillMaxWidth().testTag("signin:code"),
                    )
                }
            }
        },
        confirmButton = {
            if (sentTo == null) {
                JsButton("Send link", onClick = { onSend(email) }, style = JsButtonStyle.Text, enabled = validEmail && !busy)
            } else {
                JsButton("Sign in", onClick = { onVerify(code) }, style = JsButtonStyle.Text, enabled = code.length == 6 && !busy)
            }
        },
        dismissButton = { JsButton("Cancel", onClick = onDismiss, style = JsButtonStyle.Text) },
        shape = JsTheme.shapes.card,
        containerColor = JsTheme.colors.surfaceContainerHigh,
    )
}

@Composable
private fun JoinDialog(busy: Boolean, onJoin: (String) -> Unit, onDismiss: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    val ready = code.count(Char::isLetterOrDigit) == 10
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join a cook book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("On the other phone: Settings › Invite the other phone. Enter the code it shows.")
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().take(11) },
                    label = { Text("Invite code") },
                    placeholder = { Text("ABCDE-FGHJK") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (ready && !busy) onJoin(code) }),
                    modifier = Modifier.fillMaxWidth().testTag("join:code"),
                )
            }
        },
        confirmButton = { JsButton("Join", onClick = { onJoin(code) }, style = JsButtonStyle.Text, enabled = ready && !busy) },
        dismissButton = { JsButton("Cancel", onClick = onDismiss, style = JsButtonStyle.Text) },
        shape = JsTheme.shapes.card,
        containerColor = JsTheme.colors.surfaceContainerHigh,
    )
}

@Composable
private fun InviteDialog(code: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    code,
                    style = JsTheme.typography.displaySmall,
                    color = JsTheme.extendedColors.brandText,
                    modifier = Modifier
                        .testTag("invite:code")
                        .semantics { contentDescription = "Invite code " + code.toCharArray().joinToString(" ") },
                )
                Text("On the other phone: Settings › Join with a code. Works once, for 7 days.")
            }
        },
        confirmButton = {
            JsButton("Share", onClick = {
                val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "Join my cook book in J's Cook Book with this code: $code")
                context.startActivity(Intent.createChooser(send, null))
            }, style = JsButtonStyle.Text)
        },
        dismissButton = {
            JsButton("Copy", onClick = {
                scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Invite code", code))) }
                onDismiss()
            }, style = JsButtonStyle.Text)
        },
        shape = JsTheme.shapes.card,
        containerColor = JsTheme.colors.surfaceContainerHigh,
    )
}
