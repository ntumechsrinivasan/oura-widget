package com.srini.ourawidget

import android.content.Context

object WidgetStatsCache {
    private const val PREFS_NAME = "oura_widget_cache"
    private const val KEY_STEPS = "steps"
    private const val KEY_CALORIES = "calories"

    fun save(context: Context, steps: Int, calories: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_STEPS, steps)
            .putInt(KEY_CALORIES, calories)
            .apply()
    }

    fun stepsOrNull(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_STEPS)) prefs.getInt(KEY_STEPS, 0) else null
    }

    fun caloriesOrNull(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_CALORIES)) prefs.getInt(KEY_CALORIES, 0) else null
    }
}
