package app.jscookbook.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

/** Fraunces for display, headline and title-large; DM Sans for everything a finger touches. */
internal val JsTypography = Typography(
    displayLarge = style(FrauncesDisplay, FontWeight.SemiBold, 57.sp, 62.sp, (-0.75).sp),
    displayMedium = style(FrauncesDisplay, FontWeight.SemiBold, 45.sp, 50.sp, (-0.5).sp),
    displaySmall = style(FrauncesDisplay, FontWeight.SemiBold, 36.sp, 42.sp, (-0.25).sp),
    headlineLarge = style(FrauncesText, FontWeight.SemiBold, 32.sp, 38.sp, (-0.25).sp),
    headlineMedium = style(FrauncesText, FontWeight.SemiBold, 28.sp, 34.sp),
    headlineSmall = style(FrauncesText, FontWeight.SemiBold, 24.sp, 30.sp),
    titleLarge = style(FrauncesText, FontWeight.SemiBold, 22.sp, 28.sp),
    titleMedium = style(DmSans, FontWeight.SemiBold, 16.sp, 22.sp, 0.1.sp),
    titleSmall = style(DmSans, FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
    bodyLarge = style(DmSans, FontWeight.Normal, 16.sp, 24.sp, 0.15.sp),
    bodyMedium = style(DmSans, FontWeight.Normal, 14.sp, 20.sp, 0.2.sp),
    bodySmall = style(DmSans, FontWeight.Normal, 12.sp, 16.sp, 0.3.sp),
    labelLarge = style(DmSans, FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
    labelMedium = style(DmSans, FontWeight.SemiBold, 12.sp, 16.sp, 0.4.sp),
    labelSmall = style(DmSans, FontWeight.Medium, 11.sp, 16.sp, 0.5.sp),
)

/** The serif styles carry the title color so headings are right without passing a color. */
internal fun Typography.withTitleColor(title: Color): Typography = copy(
    displayLarge = displayLarge.copy(color = title),
    displayMedium = displayMedium.copy(color = title),
    displaySmall = displaySmall.copy(color = title),
    headlineLarge = headlineLarge.copy(color = title),
    headlineMedium = headlineMedium.copy(color = title),
    headlineSmall = headlineSmall.copy(color = title),
    titleLarge = titleLarge.copy(color = title),
)

/** Every style with its Material role name, for the design-system screen. */
val Typography.namedStyles: List<Pair<String, TextStyle>>
    get() = listOf(
        "displayLarge" to displayLarge,
        "displayMedium" to displayMedium,
        "displaySmall" to displaySmall,
        "headlineLarge" to headlineLarge,
        "headlineMedium" to headlineMedium,
        "headlineSmall" to headlineSmall,
        "titleLarge" to titleLarge,
        "titleMedium" to titleMedium,
        "titleSmall" to titleSmall,
        "bodyLarge" to bodyLarge,
        "bodyMedium" to bodyMedium,
        "bodySmall" to bodySmall,
        "labelLarge" to labelLarge,
        "labelMedium" to labelMedium,
        "labelSmall" to labelSmall,
    )
