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
                WidgetStatsCache.save(context, stats.steps, stats.totalCalories)
                views.setTextViewText(R.id.widget_steps, "${stats.steps}")
                views.setTextViewText(R.id.widget_calories, "${stats.totalCalories}")
                views.setTextViewText(R.id.widget_status, "Updated just now")
            } else {
                val cachedSteps = WidgetStatsCache.stepsOrNull(context)
                val cachedCalories = WidgetStatsCache.caloriesOrNull(context)
                views.setTextViewText(R.id.widget_steps, cachedSteps?.toString() ?: "--")
                views.setTextViewText(R.id.widget_calories, cachedCalories?.toString() ?: "--")
                views.setTextViewText(
                    R.id.widget_status,
                    if (cachedSteps != null) "Couldn't refresh, showing last known" else "Couldn't refresh yet"
                )
            }
            views.setOnClickPendingIntent(R.id.widget_root, OuraWidgetProvider.openOuraAppPendingIntent(context))
            manager.updateAppWidget(id, views)
        }

        return if (stats != null) Result.success() else Result.retry()
    }
}
