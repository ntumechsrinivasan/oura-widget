package com.srini.ourawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OuraWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.srini.ourawidget.ACTION_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, OuraWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                onUpdateWidgets(context, manager, ids)
            }
        }

        private fun onUpdateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            // Show a lightweight "refreshing" state immediately, then fetch in background.
            for (id in ids) {
                val loadingViews = RemoteViews(context.packageName, R.layout.widget_oura)
                loadingViews.setTextViewText(R.id.widget_status, "Refreshing…")
                manager.updateAppWidget(id, loadingViews)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val stats = OuraRepository.fetchTodayStats()
                for (id in ids) {
                    val views = RemoteViews(context.packageName, R.layout.widget_oura)
                    if (stats != null) {
                        views.setTextViewText(R.id.widget_steps, "${stats.steps}")
                        views.setTextViewText(R.id.widget_calories, "${stats.totalCalories}")
                        views.setTextViewText(R.id.widget_status, "Updated just now")
                    } else {
                        views.setTextViewText(R.id.widget_steps, "--")
                        views.setTextViewText(R.id.widget_calories, "--")
                        views.setTextViewText(R.id.widget_status, "Couldn't refresh, tap to retry")
                    }

                    val refreshIntent = Intent(context, OuraWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    manager.updateAppWidget(id, views)
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        onUpdateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAllWidgets(context)
        }
    }
}
