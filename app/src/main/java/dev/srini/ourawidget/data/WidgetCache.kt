package dev.srini.ourawidget.data

import android.content.Context

// Last-known-good snapshot so the widget renders instantly on process restart instead of blank.
class WidgetCache(context: Context) {

    private val prefs = context.getSharedPreferences("oura_widget_cache", Context.MODE_PRIVATE)

    fun save(snapshot: OuraSnapshot) {
        prefs.edit()
            .putInt(KEY_STEPS, snapshot.steps)
            .putInt(KEY_CALORIES, snapshot.activeCalories)
            .putLong(KEY_FETCHED_AT, snapshot.fetchedAtEpochMillis)
            .apply()
    }

    fun load(): OuraSnapshot? {
        if (!prefs.contains(KEY_FETCHED_AT)) return null
        return OuraSnapshot(
            steps = prefs.getInt(KEY_STEPS, 0),
            activeCalories = prefs.getInt(KEY_CALORIES, 0),
            fetchedAtEpochMillis = prefs.getLong(KEY_FETCHED_AT, 0L)
        )
    }

    private companion object {
        const val KEY_STEPS = "steps"
        const val KEY_CALORIES = "calories"
        const val KEY_FETCHED_AT = "fetched_at"
    }
}
