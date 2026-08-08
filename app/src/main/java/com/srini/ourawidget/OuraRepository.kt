package com.srini.ourawidget

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

data class OuraDailyStats(
    val steps: Int,
    val totalCalories: Int,
    val activeCalories: Int,
    val date: String
)

object OuraRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches today's steps + calories from the Oura v2 daily_activity endpoint.
     * Returns null on any failure (network, auth, parsing) so callers can show
     * a friendly "couldn't refresh" state instead of crashing.
     */
    fun fetchTodayStats(): OuraDailyStats? {
        val token = BuildConfig.OURA_ACCESS_TOKEN
        if (token.isBlank()) return null

        val today = LocalDate.now().toString() // yyyy-MM-dd
        val url = "https://api.ouraring.com/v2/usercollection/daily_activity" +
                "?start_date=$today&end_date=$today"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val data = json.optJSONArray("data") ?: return null
                if (data.length() == 0) {
                    // No data yet for today (e.g. ring hasn't synced) - treat as zeroed-out.
                    return OuraDailyStats(steps = 0, totalCalories = 0, activeCalories = 0, date = today)
                }
                val entry = data.getJSONObject(0)
                OuraDailyStats(
                    steps = entry.optInt("steps", 0),
                    totalCalories = entry.optInt("total_calories", 0),
                    activeCalories = entry.optInt("active_calories", 0),
                    date = today
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
