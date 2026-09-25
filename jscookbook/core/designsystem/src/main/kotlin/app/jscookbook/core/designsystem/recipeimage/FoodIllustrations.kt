package app.jscookbook.core.designsystem.recipeimage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.icon.circle
import app.jscookbook.core.designsystem.icon.ellipse
import app.jscookbook.core.designsystem.icon.roundRect

/**
 * Simple two-tone food illustrations on a 48 dp grid: line work plus a soft wash, drawn in black
 * so a single tint (ColorFilter.tint) colors both while keeping the wash translucent.
 */
val FoodIllustration.vector: ImageVector
    get() = FoodIllustrationVectors.cache.getOrPut(this) { build(this) }

private object FoodIllustrationVectors {
    val cache = HashMap<FoodIllustration, ImageVector>()
}

private fun build(illustration: FoodIllustration): ImageVector = when (illustration) {
    FoodIllustration.Bowl -> art("Bowl") {
        ground(24f, 43.5f, 13f)
        wash {
            moveTo(7f, 24f); lineTo(41f, 24f)
            curveTo(41f, 32.5f, 33.5f, 38.5f, 24f, 38.5f); curveTo(14.5f, 38.5f, 7f, 32.5f, 7f, 24f); close()
        }
        wash { moveTo(10.5f, 24f); curveTo(13f, 19.5f, 35f, 19.5f, 37.5f, 24f); close() }
        line {
            moveTo(7f, 24f); lineTo(41f, 24f)
            curveTo(41f, 32.5f, 33.5f, 38.5f, 24f, 38.5f); curveTo(14.5f, 38.5f, 7f, 32.5f, 7f, 24f); close()
            moveTo(19f, 41f); lineTo(29f, 41f)
        }
        steam(18f, 17f, 11f); steam(24f, 16f, 8f); steam(30f, 17f, 11f)
    }

    FoodIllustration.Plate -> art("Plate") {
        wash { circle(24f, 25f, 11f) }
        line { circle(24f, 25f, 17f) }
        line(width = 1.6f) { circle(24f, 25f, 11f) }
        line {
            moveTo(21f, 29f); curveTo(21f, 23f, 26f, 19.5f, 31f, 19.5f)
            curveTo(31f, 24.5f, 27f, 29f, 21f, 29f); close()
            moveTo(21f, 29f); lineTo(25.5f, 24.5f)
        }
    }

    FoodIllustration.Skillet -> art("Skillet") {
        ground(21f, 41.5f, 15f)
        // Three-quarter view: body, rim, angled handle, and a fried egg sitting in the pan.
        wash {
            moveTo(5f, 26f); curveTo(5f, 33f, 11f, 37.5f, 20f, 37.5f); curveTo(29f, 37.5f, 35f, 33f, 35f, 26f); close()
        }
        wash { ellipse(20f, 26f, 15f, 5.5f) }
        line {
            moveTo(5f, 26f); curveTo(5f, 33f, 11f, 37.5f, 20f, 37.5f); curveTo(29f, 37.5f, 35f, 33f, 35f, 26f)
            ellipse(20f, 26f, 15f, 5.5f)
        }
        line(width = 3.4f) { moveTo(35.5f, 24.5f); lineTo(44f, 20.5f) }
        // Egg white with the yolk cut out, so the pan's wash shows through as the yolk.
        path(fill = Ink, pathFillType = PathFillType.EvenOdd) {
            moveTo(11.5f, 25.8f); curveTo(11.5f, 23.2f, 16.5f, 21.8f, 20.5f, 22.1f)
            curveTo(25.5f, 22.4f, 28.8f, 24.2f, 27.4f, 26.5f); curveTo(26f, 28.8f, 12.2f, 29.6f, 11.5f, 25.8f); close()
            ellipse(19.5f, 25.2f, 3.2f, 2.1f)
        }
    }

    FoodIllustration.Whisk -> art("Whisk") {
        wash { roundRect(21.5f, 30f, 26.5f, 43f, 2.5f) }
        line { roundRect(21.5f, 30f, 26.5f, 43f, 2.5f) }
        line(width = 1.8f) {
            moveTo(22f, 30f); curveTo(11f, 20f, 15.5f, 5.5f, 24f, 5.5f); curveTo(32.5f, 5.5f, 37f, 20f, 26f, 30f)
            moveTo(23f, 30f); curveTo(17.5f, 20f, 19.5f, 8f, 24f, 8f); curveTo(28.5f, 8f, 30.5f, 20f, 25f, 30f)
            moveTo(24f, 30f); lineTo(24f, 7f)
        }
    }

    FoodIllustration.Cup -> art("Cup") {
        ground(22f, 43.5f, 14f)
        wash {
            moveTo(11f, 26f); lineTo(33f, 26f); lineTo(33f, 36f)
            curveTo(33f, 38.2f, 31.2f, 40f, 29f, 40f); lineTo(15f, 40f)
            curveTo(12.8f, 40f, 11f, 38.2f, 11f, 36f); close()
        }
        line {
            moveTo(11f, 19f); lineTo(33f, 19f); lineTo(33f, 36f)
            curveTo(33f, 38.2f, 31.2f, 40f, 29f, 40f); lineTo(15f, 40f)
            curveTo(12.8f, 40f, 11f, 38.2f, 11f, 36f); close()
            moveTo(33f, 23.5f); curveTo(40f, 23f, 41f, 33f, 33f, 34f)
        }
        steam(18f, 14.5f, 7.5f); steam(26f, 14.5f, 7.5f)
    }

    FoodIllustration.Cookie -> art("Cookie") {
        wash { circle(24f, 24f, 15f) }
        line { circle(24f, 24f, 15f) }
        solid {
            circle(18f, 18.5f, 2.2f)
            circle(28.5f, 16.5f, 1.8f)
            circle(30.5f, 27.5f, 2.3f)
            circle(19.5f, 30.5f, 1.8f)
            circle(24.5f, 23.5f, 1.4f)
        }
    }

    FoodIllustration.Pot -> art("Pot") {
        ground(24f, 44f, 16f)
        wash {
            moveTo(9f, 21f); lineTo(39f, 21f); lineTo(39f, 35f)
            curveTo(39f, 38.3f, 36.3f, 41f, 33f, 41f); lineTo(15f, 41f)
            curveTo(11.7f, 41f, 9f, 38.3f, 9f, 35f); close()
        }
        line {
            moveTo(9f, 21f); lineTo(39f, 21f); lineTo(39f, 35f)
            curveTo(39f, 38.3f, 36.3f, 41f, 33f, 41f); lineTo(15f, 41f)
            curveTo(11.7f, 41f, 9f, 38.3f, 9f, 35f); close()
            moveTo(9f, 25f); lineTo(5.5f, 25f); lineTo(5.5f, 29f); lineTo(9f, 29f)
            moveTo(39f, 25f); lineTo(42.5f, 25f); lineTo(42.5f, 29f); lineTo(39f, 29f)
        }
        line(width = 2.6f) { moveTo(6.5f, 21f); lineTo(41.5f, 21f) }
        steam(17f, 16f, 8.5f); steam(24f, 15f, 6f); steam(31f, 16f, 8.5f)
    }

    FoodIllustration.Cupcake -> art("Cupcake") {
        ground(24f, 44f, 11f)
        wash { moveTo(13f, 28f); lineTo(35f, 28f); lineTo(32f, 41f); lineTo(16f, 41f); close() }
        line {
            moveTo(13f, 28f); lineTo(35f, 28f); lineTo(32f, 41f); lineTo(16f, 41f); close()
            moveTo(19.5f, 28f); lineTo(20.5f, 41f)
            moveTo(24f, 28f); lineTo(24f, 41f)
            moveTo(28.5f, 28f); lineTo(27.5f, 41f)
        }
        line {
            moveTo(11f, 28f); curveTo(9f, 23f, 13.5f, 19f, 17.5f, 19.5f)
            curveTo(17.5f, 14.5f, 23f, 11.5f, 27.5f, 14f)
            curveTo(32.5f, 14.5f, 38f, 21f, 37f, 28f); close()
        }
        solid { circle(24f, 10.5f, 2.6f) }
        line(width = 1.6f) { moveTo(24.5f, 8f); curveTo(25f, 6f, 26.5f, 5f, 28f, 4.5f) }
    }

    FoodIllustration.Glass -> art("Glass") {
        ground(24f, 44f, 10f)
        wash { moveTo(14.6f, 21f); lineTo(33.4f, 21f); lineTo(31f, 41f); lineTo(17f, 41f); close() }
        line {
            moveTo(13.5f, 12f); lineTo(34.5f, 12f); lineTo(31f, 41f); lineTo(17f, 41f); close()
            moveTo(27f, 31f); lineTo(30.5f, 8f); lineTo(35.5f, 5f)
        }
        line(width = 1.6f) { roundRect(18.5f, 26f, 24.5f, 32f, 1.5f) }
    }

    FoodIllustration.Jar -> art("Jar") {
        ground(24f, 44.5f, 13f)
        wash {
            moveTo(17f, 15f); lineTo(31f, 15f); curveTo(35.5f, 17.5f, 37f, 20f, 37f, 24f); lineTo(37f, 37f)
            curveTo(37f, 39.8f, 34.8f, 42f, 32f, 42f); lineTo(16f, 42f)
            curveTo(13.2f, 42f, 11f, 39.8f, 11f, 37f); lineTo(11f, 24f); curveTo(11f, 20f, 12.5f, 17.5f, 17f, 15f); close()
        }
        line {
            moveTo(17f, 15f); lineTo(31f, 15f); curveTo(35.5f, 17.5f, 37f, 20f, 37f, 24f); lineTo(37f, 37f)
            curveTo(37f, 39.8f, 34.8f, 42f, 32f, 42f); lineTo(16f, 42f)
            curveTo(13.2f, 42f, 11f, 39.8f, 11f, 37f); lineTo(11f, 24f); curveTo(11f, 20f, 12.5f, 17.5f, 17f, 15f); close()
        }
        solid { roundRect(15f, 8f, 33f, 14f, 2f) }
        line(width = 1.6f) { roundRect(15.5f, 26f, 32.5f, 35f, 2f) }
    }

    FoodIllustration.Loaf -> art("Loaf") {
        ground(24f, 43.5f, 17f)
        wash { loaf() }
        line { loaf() }
        line(width = 1.8f) {
            moveTo(15f, 22.5f); curveTo(16.2f, 20.4f, 17.6f, 19f, 19.5f, 18f)
            moveTo(22.5f, 22.5f); curveTo(23.7f, 20.4f, 25.1f, 19f, 27f, 18f)
            moveTo(30f, 22.5f); curveTo(31.2f, 20.4f, 32.6f, 19f, 34.5f, 18f)
        }
    }

    FoodIllustration.Egg -> art("Egg") {
        ground(24f, 44f, 10f)
        wash { egg() }
        line { egg() }
        line(width = 1.6f) { moveTo(17.5f, 27f); curveTo(17.5f, 22f, 19.2f, 17.8f, 21.5f, 15.5f) }
    }
}

private val Ink = SolidColor(Color.Black)

private inline fun art(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f,
    ).apply(block).build()

private fun ImageVector.Builder.line(width: Float = 2.2f, pathBuilder: PathBuilder.() -> Unit) = path(
    stroke = Ink,
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = pathBuilder,
)

private fun ImageVector.Builder.wash(pathBuilder: PathBuilder.() -> Unit) =
    path(fill = Ink, fillAlpha = 0.26f, pathBuilder = pathBuilder)

private fun ImageVector.Builder.solid(pathBuilder: PathBuilder.() -> Unit) =
    path(fill = Ink, pathBuilder = pathBuilder)

/** A soft contact shadow under the object. */
private fun ImageVector.Builder.ground(cx: Float, cy: Float, rx: Float) =
    path(fill = Ink, fillAlpha = 0.12f) { ellipse(cx, cy, rx, 1.6f) }

/** One S-curve of steam rising from [bottom] to [top]. */
private fun ImageVector.Builder.steam(x: Float, bottom: Float, top: Float) = line(width = 1.8f) {
    moveTo(x, bottom)
    curveTo(x - 3f, bottom - (bottom - top) * 0.3f, x + 3f, bottom - (bottom - top) * 0.7f, x, top)
}

private fun PathBuilder.loaf() {
    moveTo(7f, 35.5f); lineTo(7f, 26f)
    curveTo(7f, 17f, 15f, 13f, 24f, 13f); curveTo(33f, 13f, 41f, 17f, 41f, 26f); lineTo(41f, 35.5f)
    curveTo(41f, 37.7f, 39.2f, 39.5f, 37f, 39.5f); lineTo(11f, 39.5f)
    curveTo(8.8f, 39.5f, 7f, 37.7f, 7f, 35.5f); close()
}

private fun PathBuilder.egg() {
    moveTo(24f, 7f)
    curveTo(32f, 7f, 37f, 21f, 37f, 28f); curveTo(37f, 36f, 31f, 41f, 24f, 41f)
    curveTo(17f, 41f, 11f, 36f, 11f, 28f); curveTo(11f, 21f, 16f, 7f, 24f, 7f); close()
}
