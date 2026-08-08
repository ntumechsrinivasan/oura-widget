package com.srini.ourawidget

import android.content.Context

object WidgetStatsCache {
    private const val PREFS_NAME = "oura_widget_cache"
    private const val KEY_STEPS = "steps"
    private const val KEY_ACTIVE_CALORIES = "active_calories"
    private const val KEY_TARGET_CALORIES = "target_calories"

    fun save(context: Context, steps: Int, activeCalories: Int, targetCalories: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_STEPS, steps)
            .putInt(KEY_ACTIVE_CALORIES, activeCalories)
            .putInt(KEY_TARGET_CALORIES, targetCalories)
            .apply()
    }

    fun stepsOrNull(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_STEPS)) prefs.getInt(KEY_STEPS, 0) else null
    }

    fun activeCaloriesOrNull(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_ACTIVE_CALORIES)) prefs.getInt(KEY_ACTIVE_CALORIES, 0) else null
    }

    fun targetCaloriesOrNull(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_TARGET_CALORIES)) prefs.getInt(KEY_TARGET_CALORIES, 0) else null
    }
}
