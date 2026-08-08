package com.srini.ourawidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OuraWidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, OuraWidgetProvider::class.java))
        if (ids.isEmpty()) return Result.success()

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
            views.setOnClickPendingIntent(R.id.widget_root, OuraWidgetProvider.refreshPendingIntent(context))
            manager.updateAppWidget(id, views)
        }

        return if (stats != null) Result.success() else Result.retry()
    }
}
