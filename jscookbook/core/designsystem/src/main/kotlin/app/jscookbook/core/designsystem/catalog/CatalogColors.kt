package app.jscookbook.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.CardPalettes
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.recipeimage.FallbackTints
import app.jscookbook.core.designsystem.theme.BM
import app.jscookbook.core.designsystem.theme.Derived
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.Swatch
import app.jscookbook.core.designsystem.theme.Swatches
import app.jscookbook.core.designsystem.theme.contrastRatio

internal fun LazyListScope.colorSection() {
    catalogHeader("bm", "Benjamin Moore", "The twelve paints. Codes and hex values as published.")
    items(Swatches.benjaminMoore.chunked(2), key = { "bm:" + it.first().name }) { pair ->
        SwatchRow(pair)
    }
    catalogHeader("derived", "Derived", "Not paints: steps built from the BM hues, mostly for dark mode.")
    items(Swatches.derived.chunked(2), key = { "derived:" + it.first().name }) { pair ->
        SwatchRow(pair)
    }
    catalogHeader("roles", "Current scheme", "What Material components see in this mode.")
    item(key = "roles") { SchemeRoles() }
}

@Composable
private fun SwatchRow(swatches: List<Swatch>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        swatches.forEach { swatch -> SwatchCard(swatch, Modifier.weight(1f)) }
        if (swatches.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SwatchCard(swatch: Swatch, modifier: Modifier) {
    JsCard(modifier.semantics(mergeDescendants = true) {}) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(swatch.color),
        )
        Column(Modifier.padding(12.dp)) {
            Text(swatch.name, style = JsTheme.typography.titleSmall)
            Text(
                listOfNotNull(swatch.code ?: "Derived", swatch.hex).joinToString(" · "),
                style = JsTheme.typography.labelMedium,
                color = JsTheme.colors.onSurfaceVariant,
            )
            Text(swatch.role, style = JsTheme.typography.bodySmall, color = JsTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SchemeRoles() {
    val c = JsTheme.colors
    val e = JsTheme.extendedColors
    val roles = listOf(
        "primary" to c.primary, "onPrimary" to c.onPrimary,
        "primaryContainer" to c.primaryContainer, "onPrimaryContainer" to c.onPrimaryContainer,
        "secondary" to c.secondary, "secondaryContainer" to c.secondaryContainer,
        "tertiary" to c.tertiary, "background" to c.background,
        "onBackground" to c.onBackground, "surfaceContainer" to c.surfaceContainer,
        "surfaceContainerHigh" to c.surfaceContainerHigh, "surfaceVariant" to c.surfaceVariant,
        "onSurfaceVariant" to c.onSurfaceVariant, "outline" to c.outline,
        "brand" to e.brand, "brandText" to e.brandText, "title" to e.title, "navIndicator" to e.navIndicator,
    )
    JsCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            roles.forEach { (name, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(JsTheme.shapes.pill)
                            .background(color)
                            .border(1.dp, c.outlineVariant, JsTheme.shapes.pill),
                    )
                    Text(name, style = JsTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(start = 12.dp))
                    Text(color.hex(), style = JsTheme.typography.labelMedium, color = c.onSurfaceVariant)
                }
            }
        }
    }
}

internal fun LazyListScope.contrastSection() {
    catalogHeader("contrast", "Contrast", "Computed live with WCAG relative luminance. AA body text needs 4.5:1; large text 3:1.")
    item(key = "contrast") {
        val pairs = listOf(
            Triple("Terra Cotta Tile on Cloud White", BM.TerraCottaTile, BM.CloudWhite),
            Triple("White on Terra Cotta Tile", Derived.White, BM.TerraCottaTile),
            Triple("Iron Mountain on Cloud White", BM.IronMountain, BM.CloudWhite),
            Triple("Rustique on Cloud White (headlines)", BM.Rustique, BM.CloudWhite),
            Triple("Audubon Russet on Cloud White", BM.AudubonRusset, BM.CloudWhite),
            Triple("Antique Pewter on Cloud White", BM.AntiquePewter, BM.CloudWhite),
            Triple("Dove Wing on Warm Charcoal", BM.DoveWing, Derived.WarmCharcoal),
            Triple("Terra Cotta Lifted on Warm Charcoal", Derived.TerraCottaLifted, Derived.WarmCharcoal),
            Triple("Georgetown Pink Beige on Warm Charcoal", BM.GeorgetownPinkBeige, Derived.WarmCharcoal),
            Triple("Venetian Portico on Warm Charcoal", BM.VenetianPortico, Derived.WarmCharcoal),
        ) + CardPalettes.all.map { Triple("Card text on ${it.name}", it.content, it.container) } +
            FallbackTints.all.map { Triple("Art initial on ${it.name}", it.ink, it.container) }
        JsCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pairs.forEach { (label, fg, bg) ->
                    val ratio = contrastRatio(fg, bg)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .clip(JsTheme.shapes.pill)
                                .background(bg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Aa", color = fg, style = JsTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(label, style = JsTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                        Text(
                            "%.1f:1 %s".format(ratio, grade(ratio)),
                            style = JsTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun grade(ratio: Float) = when {
    ratio >= 7f -> "AAA"
    ratio >= 4.5f -> "AA"
    ratio >= 3f -> "Large"
    else -> "—"
}

internal fun Color.hex(): String {
    val argb = toArgb()
    return if (alpha >= 1f) "#%06X".format(argb and 0xFFFFFF) else "#%08X".format(argb)
}
