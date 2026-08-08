package com.srini.ourawidget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OuraNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "oura_stats_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        // Always refresh the widget too, so both stay in sync.
        OuraWidgetProvider.updateAllWidgets(applicationContext)

        val stats = OuraRepository.fetchTodayStats() ?: return Result.retry()

        postNotification(stats)
        return Result.success()
    }

    private fun postNotification(stats: OuraDailyStats) {
        val context = applicationContext
        createChannelIfNeeded(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Oura update")
            .setContentText("${stats.steps} steps · ${stats.totalCalories} kcal today")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        // Android 13+ requires the POST_NOTIFICATIONS runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Oura Stats",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Periodic steps and calorie updates from your Oura ring"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
