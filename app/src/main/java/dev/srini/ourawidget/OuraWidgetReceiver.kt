package dev.srini.ourawidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dev.srini.ourawidget.work.WidgetUpdateScheduler

class OuraWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OuraWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateScheduler.ensurePeriodicRefresh(context)
        WidgetUpdateScheduler.refreshNow(context)
    }
}
