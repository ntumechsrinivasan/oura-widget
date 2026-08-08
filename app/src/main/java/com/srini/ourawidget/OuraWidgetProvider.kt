package com.srini.ourawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class OuraWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.srini.ourawidget.ACTION_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, OuraWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            // Show a lightweight "refreshing" state immediately; this part is
            // synchronous and safe to do directly.
            for (id in ids) {
                val loadingViews = RemoteViews(context.packageName, R.layout.widget_oura)
                loadingViews.setTextViewText(R.id.widget_status, "Refreshing…")
                loadingViews.setOnClickPendingIntent(R.id.widget_root, refreshPendingIntent(context))
                manager.updateAppWidget(id, loadingViews)
            }

            // The actual network fetch + final update runs via WorkManager rather
            // than a bare coroutine, so it reliably completes even if this
            // BroadcastReceiver's process gets killed right after returning.
            val request = OneTimeWorkRequestBuilder<OuraWidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun refreshPendingIntent(context: Context): PendingIntent {
            val refreshIntent = Intent(context, OuraWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            return PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAllWidgets(context)
        }
    }
}
