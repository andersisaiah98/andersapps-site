package app.jscookbook.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jscookbook.BuildConfig
import app.jscookbook.core.designsystem.component.JsButton
import app.jscookbook.core.designsystem.component.JsButtonStyle
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.component.JsHaptics
import app.jscookbook.core.designsystem.component.JsSegmentedControl
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.data.ThemeMode
import app.jscookbook.ui.common.LocalMessenger
import app.jscookbook.ui.common.ScreenTitle
import app.jscookbook.ui.common.screenPadding

private const val TapsToUnlock = 7
private const val TapWindowMillis = 1_500L

@Composable
fun SettingsRoute(
    contentPadding: PaddingValues,
    onOpenDesignSystem: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messenger = LocalMessenger.current
    SettingsScreen(
        uiState = uiState,
        onThemeModeChange = viewModel::onThemeModeChange,
        onOpenDesignSystem = onOpenDesignSystem,
        showMessage = { messenger.show(it) },
        versionLabel = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        loadFontLicenses = rememberFontLicenseLoader(),
        contentPadding = contentPadding,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenDesignSystem: () -> Unit,
    showMessage: (String) -> Unit,
    versionLabel: String,
    loadFontLicenses: () -> String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var showLicenses by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .testTag("screen:settings")
            .verticalScroll(rememberScrollState())
            .padding(screenPadding(contentPadding)),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ScreenTitle("Settings")

        SettingsGroup("Account") {
            SettingsRow(
                icon = JsIcons.Person,
                title = "Sign in",
                subtitle = "Google, or an email link",
                onClick = { showMessage("Sign-in arrives with sync in Phase 2.") },
            )
            RowDivider()
            SettingsRow(
                icon = JsIcons.People,
                title = "Household sharing",
                subtitle = "One cook book on both phones",
                onClick = { showMessage("Household sharing arrives in Phase 2.") },
            )
        }

        SettingsGroup("Appearance") {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(JsIcons.Contrast)
                    Text("Theme", style = JsTheme.typography.titleMedium, modifier = Modifier.padding(start = 14.dp))
                }
                JsSegmentedControl(
                    options = ThemeMode.entries.map { it.name },
                    selectedIndex = uiState.themeMode.ordinal,
                    onSelect = { onThemeModeChange(ThemeMode.entries[it]) },
                    modifier = Modifier.fillMaxWidth().testTag("settings:theme"),
                )
            }
        }

        SettingsGroup("Your data") {
            SettingsRow(
                icon = JsIcons.Export,
                title = "Export & backup",
                subtitle = "Everything as JSON plus a photos zip",
                onClick = { showMessage("Export arrives in a later phase.") },
            )
            RowDivider()
            SettingsRow(
                icon = JsIcons.Cloud,
                title = "Sync",
                subtitle = "This phone only for now",
                onClick = null,
                trailing = { StatusDot() },
            )
        }

        SettingsGroup("About") {
            VersionRow(versionLabel = versionLabel, onUnlock = onOpenDesignSystem)
            RowDivider()
            SettingsRow(
                icon = JsIcons.Info,
                title = "Font licenses",
                subtitle = "Fraunces and DM Sans, SIL Open Font License",
                onClick = { showLicenses = true },
            )
        }
    }

    if (showLicenses) {
        val text = remember { loadFontLicenses() }
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            confirmButton = { JsButton("Done", onClick = { showLicenses = false }, style = JsButtonStyle.Text) },
            title = { Text("Font licenses") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(text, style = JsTheme.typography.bodySmall)
                }
            },
            shape = JsTheme.shapes.card,
            containerColor = JsTheme.colors.surfaceContainerHigh,
        )
    }
}

/** The version row. Tap it seven times to open the design-system screen. */
@Composable
private fun VersionRow(versionLabel: String, onUnlock: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val motion = JsTheme.motion
    var taps by remember { mutableIntStateOf(0) }
    var lastTapAt by remember { mutableLongStateOf(0L) }
    val remaining = TapsToUnlock - taps
    SettingsRow(
        icon = JsIcons.Sparkle,
        title = "Version",
        subtitle = null,
        onClick = {
            val now = System.currentTimeMillis()
            taps = if (now - lastTapAt <= TapWindowMillis) taps + 1 else 1
            lastTapAt = now
            if (taps >= TapsToUnlock) {
                taps = 0
                haptics.performHapticFeedback(JsHaptics.Reveal)
                onUnlock()
            } else if (taps >= 3) {
                haptics.performHapticFeedback(JsHaptics.Tick)
            }
        },
        showChevron = false,
        modifier = Modifier.testTag("settings:version"),
        subtitleContent = {
            AnimatedContent(
                targetState = if (taps >= 3) "$remaining more ${if (remaining == 1) "tap" else "taps"}…" else versionLabel,
                transitionSpec = { fadeIn(motion.snappy()) togetherWith fadeOut(motion.snappy()) },
                label = "versionHint",
            ) { label ->
                Text(label, style = JsTheme.typography.bodyMedium, color = JsTheme.colors.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = JsTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
            color = JsTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp).semantics { heading() },
        )
        JsCard(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showChevron: Boolean = onClick != null,
    trailing: (@Composable () -> Unit)? = null,
    subtitleContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, indication = ripple(), interactionSource = null, onClick = onClick)
                } else {
                    Modifier.semantics(mergeDescendants = true) {}
                },
            )
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = JsTheme.typography.titleMedium)
            when {
                subtitleContent != null -> subtitleContent()
                subtitle != null -> Text(subtitle, style = JsTheme.typography.bodyMedium, color = JsTheme.colors.onSurfaceVariant)
            }
        }
        trailing?.invoke()
        if (showChevron) {
            Icon(JsIcons.ChevronRight, contentDescription = null, tint = JsTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(JsTheme.colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = JsTheme.colors.onPrimaryContainer, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun StatusDot() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(JsTheme.colors.outline),
        )
        Text(
            "Local",
            style = JsTheme.typography.labelMedium,
            color = JsTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = JsTheme.colors.outlineVariant, modifier = Modifier.padding(start = 70.dp))
}
