package com.srini.ourawidget

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Notifications are off. You can still use the home screen widget.",
                Toast.LENGTH_LONG
            ).show()
        }
        OuraWidgetProvider.schedulePeriodicRefresh(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.status_text).text =
            "Set up: checking every 15 minutes in the background (Android's fastest allowed).\n\n" +
            "Add the widget from your home screen's widget picker (long-press home screen → Widgets → Oura Widget) " +
            "to see steps and calories at a glance."

        maybeRequestNotificationPermission()
        refreshNow()
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            OuraWidgetProvider.schedulePeriodicRefresh(this)
        }
    }

    private fun refreshNow() {
        lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) { OuraRepository.fetchTodayStats() }
            val text = if (stats != null) {
                "Today: ${stats.steps} steps · ${stats.totalCalories} kcal"
            } else {
                "Couldn't reach Oura right now - check your token or connection."
            }
            findViewById<TextView>(R.id.today_stats).text = text
        }
        OuraWidgetProvider.updateAllWidgets(this)
    }
}
