package app.jscookbook.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.theme.DmSans
import app.jscookbook.core.designsystem.theme.FrauncesDisplay
import app.jscookbook.core.designsystem.theme.FrauncesText
import app.jscookbook.core.designsystem.theme.JsElevations
import app.jscookbook.core.designsystem.theme.JsRadius
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.designsystem.theme.namedStyles
import app.jscookbook.core.designsystem.theme.softShadow

internal fun LazyListScope.typeSection() {
    catalogHeader("type", "Type", "Fraunces (optical size 96 for display, 28 for headlines) and DM Sans. Bundled, never downloaded.")
    item(key = "type") {
        JsCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                JsTheme.typography.namedStyles.forEach { (name, style) -> TypeSample(name, style) }
            }
        }
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle) {
    Column {
        Text(
            "$name · ${familyName(style.fontFamily)} ${style.fontWeight?.weight ?: 400} · " +
                "${style.fontSize.value.toInt()}/${style.lineHeight.value.toInt()}",
            style = JsTheme.typography.labelSmall,
            color = JsTheme.colors.onSurfaceVariant,
        )
        Text(
            if (name.startsWith("display") || name.startsWith("headline")) "Hi, Julia" else "Brown butter & sage",
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun familyName(family: FontFamily?): String = when (family) {
    FrauncesDisplay -> "Fraunces Display"
    FrauncesText -> "Fraunces"
    DmSans -> "DM Sans"
    else -> "System"
}

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.shapeAndElevationSection() {
    catalogHeader("shapes", "Shapes", "Rounded rects only: they clip analytically, which keeps scrolling cheap.")
    item(key = "shapes") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShapeSample("Control ${JsRadius.Control.label()}", JsTheme.shapes.control)
            ShapeSample("Tile ${JsRadius.Tile.label()}", JsTheme.shapes.tile)
            ShapeSample("Card ${JsRadius.Card.label()}", JsTheme.shapes.card)
            ShapeSample("Pill", JsTheme.shapes.pill)
        }
    }
    catalogHeader("elevation", "Elevation", "Two hardware shadows per level (contact + diffuse), tinted warm.")
    items(JsElevations.all, key = { "elev:" + it.name }) { level ->
        val shape = RoundedCornerShape(JsRadius.Tile)
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth()
                .softShadow(level, shape, JsTheme.extendedColors.shadow)
                .clip(shape)
                .background(JsTheme.colors.surfaceContainer)
                .padding(20.dp),
        ) {
            Text(
                "${level.name} · contact ${level.contact.label()}, diffuse ${level.diffuse.label()}",
                style = JsTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ShapeSample(label: String, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 96.dp, height = 72.dp)
                .clip(shape)
                .background(JsTheme.colors.primaryContainer),
        )
        Text(label, style = JsTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp).width(96.dp))
    }
}

internal fun Dp.label(): String = "${value.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }} dp"
