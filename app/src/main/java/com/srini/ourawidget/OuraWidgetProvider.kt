package com.srini.ourawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class OuraWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val OURA_PACKAGE = "com.ouraring.oura"
        private const val OURA_WEB_FALLBACK = "https://cloud.ouraring.com"
        const val ACTION_REFRESH = "com.srini.ourawidget.ACTION_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, OuraWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            // Show a lightweight "refreshing" state immediately, keeping the last
            // known numbers on screen instead of resetting to the layout's "--"
            // placeholder while the fetch is in flight.
            for (id in ids) {
                val loadingViews = RemoteViews(context.packageName, R.layout.widget_oura)
                WidgetStatsCache.stepsOrNull(context)?.let {
                    loadingViews.setTextViewText(R.id.widget_steps, "$it")
                }
                WidgetStatsCache.caloriesOrNull(context)?.let {
                    loadingViews.setTextViewText(R.id.widget_calories, "$it")
                }
                loadingViews.setTextViewText(R.id.widget_status, "Refreshing…")
                loadingViews.setOnClickPendingIntent(R.id.widget_root, openOuraAppPendingIntent(context))
                loadingViews.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent(context))
                manager.updateAppWidget(id, loadingViews)
            }

            // The actual network fetch + final update runs via WorkManager rather
            // than a bare coroutine, so it reliably completes even if this
            // BroadcastReceiver's process gets killed right after returning.
            val request = OneTimeWorkRequestBuilder<OuraWidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun schedulePeriodicRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<OuraNotificationWorker>(
                15, TimeUnit.MINUTES // WorkManager's minimum periodic interval
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "oura_periodic_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun openOuraAppPendingIntent(context: Context): PendingIntent {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(OURA_PACKAGE)
                ?: Intent(Intent.ACTION_VIEW, Uri.parse(OURA_WEB_FALLBACK))
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicRefresh(context)
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
