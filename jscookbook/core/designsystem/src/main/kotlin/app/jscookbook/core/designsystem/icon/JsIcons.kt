package app.jscookbook.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The app's own line icons on a 24 dp grid: rounded 1.8 dp strokes, with a soft fill on the
 * "active" variants. Drawn in black and tinted by Icon, so the fills keep their alpha.
 */
object JsIcons {
    val Home: ImageVector by lazy { home(active = false) }
    val HomeActive: ImageVector by lazy { home(active = true) }
    val Cookbook: ImageVector by lazy { cookbook(active = false) }
    val CookbookActive: ImageVector by lazy { cookbook(active = true) }
    val Journal: ImageVector by lazy { journal(active = false) }
    val JournalActive: ImageVector by lazy { journal(active = true) }
    val Settings: ImageVector by lazy { settings(active = false) }
    val SettingsActive: ImageVector by lazy { settings(active = true) }

    val Plus: ImageVector by lazy {
        icon("Plus") {
            line(width = 2.4f) {
                moveTo(12f, 5f); lineTo(12f, 19f)
                moveTo(5f, 12f); lineTo(19f, 12f)
            }
        }
    }

    val Close: ImageVector by lazy {
        icon("Close") {
            line(width = 2f) {
                moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f)
                moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f)
            }
        }
    }

    val Back: ImageVector by lazy {
        icon("Back", autoMirror = true) {
            line(width = 2f) {
                moveTo(19f, 12f); lineTo(5.5f, 12f)
                moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        icon("ChevronRight", autoMirror = true) {
            line(width = 2f) { moveTo(9.5f, 6f); lineTo(15.5f, 12f); lineTo(9.5f, 18f) }
        }
    }

    val Check: ImageVector by lazy {
        icon("Check") {
            line(width = 2.2f) { moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f) }
        }
    }

    val Search: ImageVector by lazy {
        icon("Search") {
            line { circle(10.5f, 10.5f, 6f) }
            line(width = 2.2f) { moveTo(15.2f, 15.2f); lineTo(20f, 20f) }
        }
    }

    val Heart: ImageVector by lazy {
        icon("Heart") {
            wash { heart() }
            line { heart() }
        }
    }

    val Clock: ImageVector by lazy {
        icon("Clock") {
            line { circle(12f, 12f, 8f) }
            line { moveTo(12f, 7.5f); lineTo(12f, 12f); lineTo(15f, 14f) }
        }
    }

    val Camera: ImageVector by lazy {
        icon("Camera") {
            wash { roundRect(3.5f, 7.5f, 20.5f, 19f, 2.5f) }
            line {
                roundRect(3.5f, 7.5f, 20.5f, 19f, 2.5f)
                moveTo(8.5f, 7.5f); lineTo(10f, 5f); lineTo(14f, 5f); lineTo(15.5f, 7.5f)
            }
            line { circle(12f, 13.2f, 3.3f) }
        }
    }

    val Folder: ImageVector by lazy {
        icon("Folder") {
            wash { folder() }
            line { folder() }
        }
    }

    val Pencil: ImageVector by lazy {
        icon("Pencil") {
            wash {
                moveTo(5f, 19f); lineTo(6f, 15f); lineTo(15.6f, 5.4f)
                curveTo(16.4f, 4.6f, 17.6f, 4.6f, 18.4f, 5.4f); lineTo(18.6f, 5.6f)
                curveTo(19.4f, 6.4f, 19.4f, 7.6f, 18.6f, 8.4f); lineTo(9f, 18f); close()
            }
            line {
                moveTo(5f, 19f); lineTo(6f, 15f); lineTo(15.6f, 5.4f)
                curveTo(16.4f, 4.6f, 17.6f, 4.6f, 18.4f, 5.4f); lineTo(18.6f, 5.6f)
                curveTo(19.4f, 6.4f, 19.4f, 7.6f, 18.6f, 8.4f); lineTo(9f, 18f); close()
                moveTo(14f, 7f); lineTo(17f, 10f)
            }
        }
    }

    val Dice: ImageVector by lazy {
        icon("Dice") {
            wash { roundRect(4.5f, 4.5f, 19.5f, 19.5f, 3.5f) }
            line { roundRect(4.5f, 4.5f, 19.5f, 19.5f, 3.5f) }
            solid {
                circle(8.8f, 8.8f, 1.4f)
                circle(12f, 12f, 1.4f)
                circle(15.2f, 15.2f, 1.4f)
            }
        }
    }

    val Bolt: ImageVector by lazy {
        icon("Bolt") {
            wash { bolt() }
            line { bolt() }
        }
    }

    val Hourglass: ImageVector by lazy {
        icon("Hourglass") {
            line {
                moveTo(7f, 4f); lineTo(17f, 4f)
                moveTo(7f, 20f); lineTo(17f, 20f)
                moveTo(8.5f, 4f); curveTo(8.5f, 9f, 12f, 10f, 12f, 12f); curveTo(12f, 14f, 8.5f, 15f, 8.5f, 20f)
                moveTo(15.5f, 4f); curveTo(15.5f, 9f, 12f, 10f, 12f, 12f); curveTo(12f, 14f, 15.5f, 15f, 15.5f, 20f)
            }
            solid { moveTo(9.8f, 19f); lineTo(14.2f, 19f); lineTo(12f, 16.2f); close() }
        }
    }

    val Cookie: ImageVector by lazy {
        icon("Cookie") {
            wash { circle(12f, 12f, 8f) }
            line { circle(12f, 12f, 8f) }
            solid {
                circle(9f, 9.5f, 1.2f)
                circle(14.5f, 8.8f, 1f)
                circle(15f, 14f, 1.3f)
                circle(10f, 15f, 1f)
            }
        }
    }

    val Person: ImageVector by lazy {
        icon("Person") {
            line {
                circle(12f, 8.5f, 3.5f)
                moveTo(5f, 20f); curveTo(5f, 16.5f, 8f, 14f, 12f, 14f); curveTo(16f, 14f, 19f, 16.5f, 19f, 20f)
            }
        }
    }

    val People: ImageVector by lazy {
        icon("People") {
            line {
                circle(9f, 8.5f, 3f)
                moveTo(3.5f, 19.5f); curveTo(3.5f, 16.2f, 6f, 14f, 9f, 14f); curveTo(12f, 14f, 14.5f, 16.2f, 14.5f, 19.5f)
                circle(16.5f, 9.5f, 2.5f)
                moveTo(16.5f, 14f); curveTo(18.8f, 14f, 20.5f, 15.8f, 20.5f, 18.5f)
            }
        }
    }

    val Contrast: ImageVector by lazy {
        icon("Contrast") {
            line { circle(12f, 12f, 8f) }
            solid {
                moveTo(12f, 4f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 12f, y1 = 20f)
                close()
            }
        }
    }

    val Export: ImageVector by lazy {
        icon("Export") {
            line {
                moveTo(12f, 14f); lineTo(12f, 4.5f)
                moveTo(8.5f, 8f); lineTo(12f, 4.5f); lineTo(15.5f, 8f)
                moveTo(5f, 13f); lineTo(5f, 17.5f)
                curveTo(5f, 18.9f, 6.1f, 20f, 7.5f, 20f); lineTo(16.5f, 20f)
                curveTo(17.9f, 20f, 19f, 18.9f, 19f, 17.5f); lineTo(19f, 13f)
            }
        }
    }

    val Info: ImageVector by lazy {
        icon("Info") {
            line { circle(12f, 12f, 8f) }
            line(width = 2f) { moveTo(12f, 11f); lineTo(12f, 16f) }
            solid { circle(12f, 8f, 1.15f) }
        }
    }

    val Cloud: ImageVector by lazy {
        icon("Cloud") {
            line {
                moveTo(7f, 18f); curveTo(4.8f, 18f, 3f, 16.2f, 3f, 14f); curveTo(3f, 11.9f, 4.6f, 10.2f, 6.6f, 10f)
                curveTo(7.3f, 7.2f, 9.4f, 5.5f, 12f, 5.5f); curveTo(15.1f, 5.5f, 17.6f, 7.9f, 17.9f, 11f)
                curveTo(19.7f, 11.3f, 21f, 12.8f, 21f, 14.6f); curveTo(21f, 16.5f, 19.5f, 18f, 17.6f, 18f); close()
            }
        }
    }

    val Sparkle: ImageVector by lazy {
        icon("Sparkle") {
            wash { sparkle() }
            line { sparkle() }
        }
    }

    /** Every icon with its name, for the design-system screen. */
    val all: List<Pair<String, ImageVector>> by lazy {
        listOf(
            "Home" to Home, "HomeActive" to HomeActive,
            "Cookbook" to Cookbook, "CookbookActive" to CookbookActive,
            "Journal" to Journal, "JournalActive" to JournalActive,
            "Settings" to Settings, "SettingsActive" to SettingsActive,
            "Plus" to Plus, "Close" to Close, "Back" to Back, "ChevronRight" to ChevronRight,
            "Check" to Check, "Search" to Search, "Heart" to Heart, "Clock" to Clock,
            "Camera" to Camera, "Folder" to Folder, "Pencil" to Pencil, "Dice" to Dice,
            "Bolt" to Bolt, "Hourglass" to Hourglass, "Cookie" to Cookie, "Person" to Person,
            "People" to People, "Contrast" to Contrast, "Export" to Export, "Info" to Info,
            "Cloud" to Cloud, "Sparkle" to Sparkle,
        )
    }

    private fun home(active: Boolean) = icon(if (active) "HomeActive" else "Home") {
        if (active) wash { house() }
        line { house() }
        line {
            moveTo(10f, 20f); lineTo(10f, 15.5f)
            curveTo(10f, 14.95f, 10.45f, 14.5f, 11f, 14.5f); lineTo(13f, 14.5f)
            curveTo(13.55f, 14.5f, 14f, 14.95f, 14f, 15.5f); lineTo(14f, 20f)
        }
    }

    private fun cookbook(active: Boolean) = icon(if (active) "CookbookActive" else "Cookbook") {
        if (active) wash { bookPages() }
        line {
            bookPages()
            moveTo(12f, 7f); lineTo(12f, 19.5f)
        }
    }

    private fun journal(active: Boolean) = icon(if (active) "JournalActive" else "Journal") {
        if (active) wash { roundRect(4f, 5.5f, 20f, 20f, 2.5f) }
        line {
            roundRect(4f, 5.5f, 20f, 20f, 2.5f)
            moveTo(8.5f, 3.5f); lineTo(8.5f, 7.5f)
            moveTo(15.5f, 3.5f); lineTo(15.5f, 7.5f)
            moveTo(4f, 10f); lineTo(20f, 10f)
        }
        line(width = 2f) { moveTo(9f, 14.8f); lineTo(11f, 16.8f); lineTo(15f, 12.8f) }
    }

    private fun settings(active: Boolean) = icon(if (active) "SettingsActive" else "Settings") {
        line {
            moveTo(4f, 7f); lineTo(6.8f, 7f); moveTo(11.2f, 7f); lineTo(20f, 7f)
            moveTo(4f, 12f); lineTo(12.8f, 12f); moveTo(17.2f, 12f); lineTo(20f, 12f)
            moveTo(4f, 17f); lineTo(5.8f, 17f); moveTo(10.2f, 17f); lineTo(20f, 17f)
        }
        if (active) {
            solid { circle(9f, 7f, 2.2f); circle(15f, 12f, 2.2f); circle(8f, 17f, 2.2f) }
        } else {
            line { circle(9f, 7f, 2.2f); circle(15f, 12f, 2.2f); circle(8f, 17f, 2.2f) }
        }
    }
}

private val Ink = SolidColor(Color.Black)

private inline fun icon(name: String, autoMirror: Boolean = false, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    ).apply(block).build()

private fun ImageVector.Builder.line(width: Float = 1.8f, pathBuilder: PathBuilder.() -> Unit) = path(
    stroke = Ink,
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = pathBuilder,
)

private fun ImageVector.Builder.wash(pathBuilder: PathBuilder.() -> Unit) =
    path(fill = Ink, fillAlpha = 0.24f, pathBuilder = pathBuilder)

private fun ImageVector.Builder.solid(pathBuilder: PathBuilder.() -> Unit) =
    path(fill = Ink, pathFillType = PathFillType.NonZero, pathBuilder = pathBuilder)

internal fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = 2 * r, dy1 = 0f)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = -2 * r, dy1 = 0f)
    close()
}

internal fun PathBuilder.ellipse(cx: Float, cy: Float, rx: Float, ry: Float) {
    moveTo(cx - rx, cy)
    arcToRelative(rx, ry, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = 2 * rx, dy1 = 0f)
    arcToRelative(rx, ry, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = -2 * rx, dy1 = 0f)
    close()
}

internal fun PathBuilder.roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
    moveTo(left + radius, top)
    lineTo(right - radius, top)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = right, y1 = top + radius)
    lineTo(right, bottom - radius)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = right - radius, y1 = bottom)
    lineTo(left + radius, bottom)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = left, y1 = bottom - radius)
    lineTo(left, top + radius)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = left + radius, y1 = top)
    close()
}

private fun PathBuilder.house() {
    moveTo(5f, 10.5f); lineTo(12f, 4.5f); lineTo(19f, 10.5f); lineTo(19f, 18.5f)
    curveTo(19f, 19.33f, 18.33f, 20f, 17.5f, 20f); lineTo(6.5f, 20f)
    curveTo(5.67f, 20f, 5f, 19.33f, 5f, 18.5f); close()
}

private fun PathBuilder.bookPages() {
    moveTo(12f, 7f); curveTo(10f, 5.5f, 7f, 5f, 4f, 5.5f); lineTo(4f, 18.5f)
    curveTo(7f, 18f, 10f, 18.3f, 12f, 19.5f); curveTo(14f, 18.3f, 17f, 18f, 20f, 18.5f)
    lineTo(20f, 5.5f); curveTo(17f, 5f, 14f, 5.5f, 12f, 7f); close()
}

private fun PathBuilder.heart() {
    moveTo(12f, 19.5f)
    curveTo(12f, 19.5f, 4f, 14.5f, 4f, 9.25f)
    curveTo(4f, 6.9f, 5.9f, 5f, 8.25f, 5f)
    curveTo(9.8f, 5f, 11.2f, 5.85f, 12f, 7.1f)
    curveTo(12.8f, 5.85f, 14.2f, 5f, 15.75f, 5f)
    curveTo(18.1f, 5f, 20f, 6.9f, 20f, 9.25f)
    curveTo(20f, 14.5f, 12f, 19.5f, 12f, 19.5f)
    close()
}

private fun PathBuilder.folder() {
    moveTo(3.5f, 7f); curveTo(3.5f, 5.9f, 4.4f, 5f, 5.5f, 5f); lineTo(9.5f, 5f); lineTo(11.5f, 7f)
    lineTo(18.5f, 7f); curveTo(19.6f, 7f, 20.5f, 7.9f, 20.5f, 9f); lineTo(20.5f, 17f)
    curveTo(20.5f, 18.1f, 19.6f, 19f, 18.5f, 19f); lineTo(5.5f, 19f)
    curveTo(4.4f, 19f, 3.5f, 18.1f, 3.5f, 17f); close()
}

private fun PathBuilder.bolt() {
    moveTo(13f, 3.5f); lineTo(6f, 13.5f); lineTo(11.5f, 13.5f); lineTo(10.5f, 20.5f)
    lineTo(18f, 10f); lineTo(12.5f, 10f); close()
}

private fun PathBuilder.sparkle() {
    moveTo(12f, 3.5f)
    curveTo(12.6f, 8.5f, 15.5f, 11.4f, 20.5f, 12f)
    curveTo(15.5f, 12.6f, 12.6f, 15.5f, 12f, 20.5f)
    curveTo(11.4f, 15.5f, 8.5f, 12.6f, 3.5f, 12f)
    curveTo(8.5f, 11.4f, 11.4f, 8.5f, 12f, 3.5f)
    close()
}
