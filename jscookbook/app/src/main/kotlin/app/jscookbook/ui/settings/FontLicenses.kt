package app.jscookbook.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private val LicenseFiles = listOf(
    "Fraunces" to "licenses/Fraunces-OFL.txt",
    "DM Sans" to "licenses/DMSans-OFL.txt",
)

/** Reads the bundled OFL texts (shipped in the APK next to the fonts) when asked. */
@Composable
fun rememberFontLicenseLoader(): () -> String {
    val assets = LocalContext.current.assets
    return remember(assets) {
        {
            LicenseFiles.joinToString("\n\n") { (name, path) ->
                "$name\n\n" + assets.open(path).bufferedReader().use { it.readText() }.trim()
            }
        }
    }
}
