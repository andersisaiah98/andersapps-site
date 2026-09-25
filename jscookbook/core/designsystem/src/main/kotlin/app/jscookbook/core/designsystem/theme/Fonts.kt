package app.jscookbook.core.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.jscookbook.core.designsystem.R

// Fraunces and DM Sans are bundled variable fonts (OFL, see assets/licenses) so first launch
// never swaps fonts and everything works offline.
//
// Fraunces has optical-size (opsz), softness (SOFT) and "wonky" (WONK) axes. Display sizes use a
// high optical size with the wonky letterforms for character; headlines use a text optical size.

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: FontWeight, style: FontStyle, opticalSize: Float, wonky: Boolean): Font =
    Font(
        resId = if (style == FontStyle.Italic) R.font.fraunces_italic else R.font.fraunces,
        weight = weight,
        style = style,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight.weight),
            FontVariation.Setting("opsz", opticalSize),
            FontVariation.Setting("SOFT", 50f),
            FontVariation.Setting("WONK", if (wonky) 1f else 0f),
        ),
    )

private fun frauncesFamily(opticalSize: Float, wonky: Boolean): FontFamily = FontFamily(
    fraunces(FontWeight.Normal, FontStyle.Normal, opticalSize, wonky),
    fraunces(FontWeight.Medium, FontStyle.Normal, opticalSize, wonky),
    fraunces(FontWeight.SemiBold, FontStyle.Normal, opticalSize, wonky),
    fraunces(FontWeight.Bold, FontStyle.Normal, opticalSize, wonky),
    fraunces(FontWeight.Normal, FontStyle.Italic, opticalSize, wonky),
    fraunces(FontWeight.SemiBold, FontStyle.Italic, opticalSize, wonky),
)

@OptIn(ExperimentalTextApi::class)
private fun dmSans(weight: FontWeight): Font = Font(
    resId = R.font.dm_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", 14f),
    ),
)

/** Fraunces tuned for big type: display styles and the fallback-art initial. */
val FrauncesDisplay: FontFamily = frauncesFamily(opticalSize = 96f, wonky = true)

/** Fraunces tuned for headlines and titles. */
val FrauncesText: FontFamily = frauncesFamily(opticalSize = 28f, wonky = false)

/** DM Sans for UI and body text. */
val DmSans: FontFamily = FontFamily(
    dmSans(FontWeight.Normal),
    dmSans(FontWeight.Medium),
    dmSans(FontWeight.SemiBold),
    dmSans(FontWeight.Bold),
)
