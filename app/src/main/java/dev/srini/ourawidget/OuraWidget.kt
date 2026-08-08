package dev.srini.ourawidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.srini.ourawidget.actions.RefreshAction
import dev.srini.ourawidget.data.OuraRepository
import dev.srini.ourawidget.data.OuraSnapshot
import dev.srini.ourawidget.ui.ConfigActivity
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class OuraWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = OuraRepository(context)
        val configured = repository.isConfigured()
        val snapshot = if (configured) repository.cachedSnapshot() else null

        provideContent {
            WidgetContent(configured = configured, snapshot = snapshot)
        }
    }
}

@Composable
private fun WidgetContent(configured: Boolean, snapshot: OuraSnapshot?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .padding(16.dp)
    ) {
        when {
            !configured -> NotConfiguredContent()
            snapshot == null -> LoadingContent()
            else -> StatsContent(snapshot)
        }
    }
}

@Composable
private fun NotConfiguredContent() {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<ConfigActivity>()),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_link),
            contentDescription = null,
            colorFilter = ColorFilter.tint(ColorProvider(R.color.brand_primary)),
            modifier = GlanceModifier.size(28.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = context.getString(R.string.widget_not_configured),
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_surface),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun LoadingContent() {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_updating),
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_surface_variant),
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun StatsContent(snapshot: OuraSnapshot) {
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.getDefault()) }

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "Today",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_on_surface_variant),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(ColorProvider(R.color.widget_surface_variant))
                    .cornerRadius(14.dp)
                    .clickable(actionRunCallback<RefreshAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = context.getString(R.string.widget_refresh_cd),
                    colorFilter = ColorFilter.tint(ColorProvider(R.color.widget_on_surface_variant)),
                    modifier = GlanceModifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatColumn(
                modifier = GlanceModifier.defaultWeight(),
                iconRes = R.drawable.ic_widget_steps,
                accentColorRes = R.color.widget_accent_steps,
                value = numberFormat.format(snapshot.steps),
                label = context.getString(R.string.widget_steps)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            StatColumn(
                modifier = GlanceModifier.defaultWeight(),
                iconRes = R.drawable.ic_widget_flame,
                accentColorRes = R.color.widget_accent_calories,
                value = numberFormat.format(snapshot.activeCalories),
                label = context.getString(R.string.widget_calories)
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "${context.getString(R.string.widget_updated_prefix)} ${relativeTime(snapshot.fetchedAtEpochMillis)}",
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_surface_variant),
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun StatColumn(
    modifier: GlanceModifier,
    iconRes: Int,
    accentColorRes: Int,
    value: String,
    label: String
) {
    Column(modifier = modifier) {
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .background(ColorProvider(R.color.widget_surface_variant))
                .cornerRadius(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ColorProvider(accentColorRes)),
                modifier = GlanceModifier.size(18.dp)
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_surface),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_surface_variant),
                fontSize = 11.sp
            )
        )
    }
}

private fun relativeTime(epochMillis: Long): String {
    val diffMs = System.currentTimeMillis() - epochMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        else -> "${TimeUnit.MILLISECONDS.toHours(diffMs)}h ago"
    }
}
