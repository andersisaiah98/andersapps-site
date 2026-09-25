package app.jscookbook.ui.journal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jscookbook.core.designsystem.component.JsCard
import app.jscookbook.core.designsystem.recipeimage.FoodIllustration
import app.jscookbook.core.designsystem.recipeimage.vector
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.ui.common.ScreenTitle
import app.jscookbook.ui.common.screenPadding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

@Composable
fun JournalScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { LocalDate.now() },
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("screen:journal"),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "title") {
            ScreenTitle("Journal", subtitle = "Every time you make something, it lands here.")
        }
        item(key = "week") { WeekStrip(today) }
        item(key = "empty") { EmptyJournal() }
    }
}

@Composable
private fun WeekStrip(today: LocalDate) {
    val monday = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val extended = JsTheme.extendedColors
    val colors = JsTheme.colors
    val spoken = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()) }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (0L until 7L).forEach { offset ->
            val day = monday.plusDays(offset)
            val isToday = day == today
            Column(
                Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = day.format(spoken) + if (isToday) ", today" else ""
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    day.dayOfWeek.getDisplayName(JavaTextStyle.NARROW, Locale.getDefault()),
                    style = JsTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (isToday) extended.brand else colors.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        day.dayOfMonth.toString(),
                        style = JsTheme.typography.titleMedium,
                        color = if (isToday) extended.onBrand else colors.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyJournal() {
    JsCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = FoodIllustration.Skillet.vector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(JsTheme.extendedColors.brandText),
                modifier = Modifier.size(120.dp),
            )
            Text(
                "Your cooking story starts here",
                style = JsTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "Tap “I made this” on any recipe to log it, with a note for next time and a photo. " +
                    "The cook log arrives in Phase 3.",
                style = JsTheme.typography.bodyMedium,
                color = JsTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
