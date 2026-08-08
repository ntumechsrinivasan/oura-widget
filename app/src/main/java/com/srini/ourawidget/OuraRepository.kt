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
    val targetCalories: Int,
    val date: String
)

object OuraRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches the most recent day's steps + calories from the Oura v2 daily_activity
     * endpoint. Queries a small window (yesterday through today) rather than just
     * today, because Oura's API frequently hasn't published today's record yet even
     * when the Oura app itself already shows synced data for today - taking the
     * latest available record avoids showing a false "0" in that gap.
     * Returns null on any failure (network, auth, parsing) so callers can show
     * a friendly "couldn't refresh" state instead of crashing.
     */
    fun fetchTodayStats(): OuraDailyStats? {
        val token = BuildConfig.OURA_ACCESS_TOKEN
        if (token.isBlank()) return null

        val today = LocalDate.now()
        val startDate = today.minusDays(1).toString() // yyyy-MM-dd
        val endDate = today.toString()
        val url = "https://api.ouraring.com/v2/usercollection/daily_activity" +
                "?start_date=$startDate&end_date=$endDate"

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
                    // Oura has no record at all in this window yet.
                    return OuraDailyStats(steps = 0, totalCalories = 0, activeCalories = 0, targetCalories = 0, date = endDate)
                }
                // Records come back oldest-first; the last one is the most recent Oura has published.
                val entry = data.getJSONObject(data.length() - 1)
                OuraDailyStats(
                    steps = entry.optInt("steps", 0),
                    totalCalories = entry.optInt("total_calories", 0),
                    activeCalories = entry.optInt("active_calories", 0),
                    targetCalories = entry.optInt("target_calories", 0),
                    date = entry.optString("day", endDate)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
