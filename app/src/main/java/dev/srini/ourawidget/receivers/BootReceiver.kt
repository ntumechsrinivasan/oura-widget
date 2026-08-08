package dev.srini.ourawidget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.srini.ourawidget.work.WidgetUpdateScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        WidgetUpdateScheduler.ensurePeriodicRefresh(context)
        WidgetUpdateScheduler.refreshNow(context)
    }
}
