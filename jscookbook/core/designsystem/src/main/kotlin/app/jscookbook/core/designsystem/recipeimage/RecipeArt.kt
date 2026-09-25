package app.jscookbook.core.designsystem.recipeimage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import app.jscookbook.core.designsystem.theme.FrauncesDisplay
import app.jscookbook.core.designsystem.theme.JsTheme
import coil3.compose.AsyncImage
import java.io.File

/** Draws a recipe's tile image: the photo when there is one, otherwise its fallback art. */
@Composable
fun RecipeArt(image: RecipeImage, modifier: Modifier = Modifier, contentDescription: String? = null) {
    when (image) {
        is RecipeImage.Fallback -> FallbackArtImage(image.art, modifier)
        is RecipeImage.Photo -> PhotoArt(image.model, modifier, contentDescription)
        is RecipeImage.Generated -> PhotoArt(image.model, modifier, contentDescription)
    }
}

@Composable
private fun PhotoArt(model: Any, modifier: Modifier, contentDescription: String?) {
    AsyncImage(
        model = if (model is String && model.startsWith("/")) File(model) else model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(JsTheme.colors.surfaceVariant),
    )
}

/**
 * Generated art for a recipe without a photo: a palette-colored field, a tilted food
 * illustration and the title's initial in Fraunces. Everything is drawn in one cached draw
 * pass (no subcomposition), so it's cheap in scrolling grids and sizes itself to any tile.
 */
@Composable
fun FallbackArtImage(
    art: FallbackArt,
    modifier: Modifier = Modifier,
    showInitial: Boolean = true,
) {
    val painter = rememberVectorPainter(art.illustration.vector)
    val textMeasurer = rememberTextMeasurer()
    val tint = art.tint
    Spacer(
        modifier.drawWithCache {
            val side = size.minDimension
            // Portrait and landscape tiles have room below/beside the initial, so the art grows.
            val roominess = ((size.maxDimension / side - 1f) / 0.25f).coerceIn(0f, 1f)
            val illustrationSide = side * (0.66f + 0.18f * roominess)
            val illustrationTopLeft = Offset(
                x = size.width - illustrationSide - side * 0.03f,
                y = size.height - illustrationSide - side * 0.03f,
            )
            val highlight = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.15f),
                radius = side * 0.9f,
            )
            val initial = if (showInitial && art.initial.isNotEmpty()) {
                textMeasurer.measure(
                    text = art.initial,
                    style = TextStyle(
                        fontFamily = FrauncesDisplay,
                        fontWeight = FontWeight.SemiBold,
                        // Sized to the tile in px, so the user's font scale doesn't distort the art.
                        fontSize = (side * 0.36f).toDp().toSp(),
                        color = tint.ink,
                    ),
                )
            } else {
                null
            }
            val filter = ColorFilter.tint(tint.ink)
            onDrawBehind {
                drawRect(tint.container)
                drawRect(highlight)
                rotate(art.tiltDegrees, pivot = illustrationTopLeft + Offset(illustrationSide / 2, illustrationSide / 2)) {
                    translate(illustrationTopLeft.x, illustrationTopLeft.y) {
                        with(painter) {
                            draw(Size(illustrationSide, illustrationSide), alpha = 0.92f, colorFilter = filter)
                        }
                    }
                }
                initial?.let { drawText(it, topLeft = Offset(side * 0.1f, side * 0.04f)) }
            }
        },
    )
}
