package app.jscookbook.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsIconButton
import app.jscookbook.core.designsystem.component.JsSegmentedControl
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.modifier.paperBackground
import app.jscookbook.core.designsystem.theme.JsTheme

/**
 * The hidden design-system screen (Settings › tap the version 7 times). Shows every token and
 * component on a real device, with a toggle to preview the other color mode in place.
 */
@Composable
fun DesignSystemCatalog(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val appIsDark = JsTheme.extendedColors.isDark
    var previewDark by rememberSaveable { mutableStateOf(appIsDark) }
    JsTheme(darkTheme = previewDark, reducedMotion = JsTheme.reducedMotion) {
        CompositionLocalProvider(LocalContentColor provides JsTheme.colors.onBackground) {
            Column(
                modifier
                    .fillMaxSize()
                    .paperBackground(JsTheme.colors.background, JsTheme.extendedColors.paperGrain)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .testTag("screen:design_system"),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JsIconButton(icon = JsIcons.Back, contentDescription = "Back", onClick = onBack)
                    Text(
                        "Design system",
                        style = JsTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() },
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "intro") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("J's Cook Book", style = JsTheme.typography.displaySmall)
                            Text(
                                "Benjamin Moore colors from Amerie Creative's TerraCotta palette, " +
                                    "Fraunces and DM Sans, springs everywhere.",
                                style = JsTheme.typography.bodyLarge,
                            )
                            JsSegmentedControl(
                                options = listOf("Light", "Dark"),
                                selectedIndex = if (previewDark) 1 else 0,
                                onSelect = { previewDark = it == 1 },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    colorSection()
                    contrastSection()
                    typeSection()
                    shapeAndElevationSection()
                    buttonSection()
                    cardSection()
                    fallbackArtSection()
                    iconSection()
                    motionSection()
                    hapticsSection()
                }
            }
        }
    }
}

internal fun LazyListScope.catalogHeader(key: String, title: String, note: String? = null) {
    item(key = "header:$key") {
        Box(Modifier.padding(top = 28.dp, bottom = 4.dp)) {
            Column {
                Text(title, style = JsTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                if (note != null) {
                    Text(
                        note,
                        style = JsTheme.typography.bodyMedium,
                        color = JsTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
