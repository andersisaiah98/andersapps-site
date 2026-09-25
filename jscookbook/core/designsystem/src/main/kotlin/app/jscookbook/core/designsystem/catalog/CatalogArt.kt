package app.jscookbook.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.recipeimage.FallbackArt
import app.jscookbook.core.designsystem.recipeimage.FallbackArtImage
import app.jscookbook.core.designsystem.recipeimage.FallbackArtSelector
import app.jscookbook.core.designsystem.recipeimage.FallbackTints
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.RecipeType
import app.jscookbook.core.designsystem.theme.JsTheme

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.fallbackArtSection() {
    catalogHeader(
        "art",
        "Fallback art",
        "No photo? A tile gets an illustration picked from its title, category and type, a palette " +
            "tint from a hash of the title, and its initial in Fraunces. Same title, same art, on both phones.",
    )
    item(key = "art-try") { TryFallbackArt() }
    item(key = "art-illustrations-title") {
        Text("Illustrations", style = JsTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    }
    items(FoodIllustration.entries.chunked(3), key = { "art-ill:" + it.first().name }) { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { illustration ->
                ArtSwatch(
                    art = FallbackArt(illustration, FallbackTints.all[illustration.ordinal % FallbackTints.all.size], "", 0f),
                    label = illustration.label,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
    item(key = "art-tints-title") {
        Text("Tints", style = JsTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    }
    items(FallbackTints.all.chunked(3), key = { "art-tint:" + it.first().name }) { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { tint ->
                ArtSwatch(
                    art = FallbackArt(FoodIllustration.Bowl, tint, tint.name.first().toString(), 0f),
                    label = tint.name,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun ArtSwatch(art: FallbackArt, label: String, modifier: Modifier) {
    Column(modifier) {
        FallbackArtImage(
            art = art,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(JsTheme.shapes.tile),
        )
        Text(
            label,
            style = JsTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TryFallbackArt() {
    var title by rememberSaveable { mutableStateOf("Grandma's Tomato Soup") }
    var type by rememberSaveable { mutableStateOf<RecipeType?>(RecipeType.Meal) }
    val art = remember(title, type) { FallbackArtSelector.select(title, type) }
    JsCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FallbackArtImage(art, Modifier.width(128.dp).aspectRatio(4f / 5f).clip(JsTheme.shapes.tile))
                Column(Modifier.weight(1f)) {
                    Text(art.illustration.label, style = JsTheme.typography.titleMedium)
                    Text(art.tint.name, style = JsTheme.typography.bodyMedium)
                    Text(
                        "Tilt %.1f°".format(art.tiltDegrees),
                        style = JsTheme.typography.bodySmall,
                        color = JsTheme.colors.onSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe title") },
                singleLine = true,
                shape = JsTheme.shapes.control,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (listOf<RecipeType?>(null) + RecipeType.entries).forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option?.label ?: "No type") },
                        leadingIcon = if (type == option) {
                            { Icon(JsIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        },
                        shape = JsTheme.shapes.pill,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.iconSection() {
    catalogHeader("icons", "Icons", "Our own line icons. Active variants add a soft fill.")
    item(key = "icons") {
        JsCard {
            FlowRow(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                JsIcons.all.forEach { (name, icon) ->
                    Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = null, tint = JsTheme.colors.onSurface)
                        Text(
                            name,
                            style = JsTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
