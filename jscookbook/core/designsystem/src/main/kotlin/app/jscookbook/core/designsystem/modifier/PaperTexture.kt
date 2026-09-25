package app.jscookbook.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Fills with [color] and lays a barely-there paper grain over it. The grain is one small tile,
 * generated once per density and color and drawn as a single repeating shader, so it costs one
 * extra rect per frame. Use it for screen backgrounds only, never per item.
 */
fun Modifier.paperBackground(color: Color, grain: Color): Modifier = drawWithCache {
    val tile = PaperGrain.tile(density = density, grain = grain)
    val brush = ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    onDrawBehind {
        drawRect(color)
        drawRect(brush)
    }
}

internal object PaperGrain {
    private const val TILE_DP = 120f
    private const val SEED = 0x7A7A
    private var cache: Triple<Float, Color, ImageBitmap>? = null

    fun tile(density: Float, grain: Color): ImageBitmap {
        cache?.let { (cachedDensity, cachedGrain, bitmap) ->
            if (cachedDensity == density && cachedGrain == grain) return bitmap
        }
        return generate(density, grain).also { cache = Triple(density, grain, it) }
    }

    private fun generate(density: Float, grain: Color): ImageBitmap {
        val size = max(64, (TILE_DP * density).roundToInt())
        val bitmap = ImageBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val random = Random(SEED)
        val speck = max(1f, density * 0.55f)
        // Light grain on dark backgrounds shows more, so it is laid on thinner.
        val maxAlpha = if (grain.luminanceGuess() > 0.5f) 0.045f else 0.08f
        repeat(size * size / 70) {
            paint.color = grain.copy(alpha = random.nextFloat() * maxAlpha)
            val x = random.nextFloat() * size
            val y = random.nextFloat() * size
            val w = speck * (0.6f + random.nextFloat())
            canvas.drawRect(x, y, x + w, y + w, paint)
        }
        return bitmap
    }

    private fun Color.luminanceGuess(): Float = (red + green + blue) / 3f
}
