package dev.srini.ourawidget.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OuraApi {
    @GET("v2/usercollection/daily_activity")
    suspend fun getDailyActivity(
        @Header("Authorization") bearerToken: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<DailyActivityResponse>

    companion object {
        const val BASE_URL = "https://api.ouraring.com/"
    }
}
