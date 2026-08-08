package dev.srini.ourawidget.data

import android.content.Context
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

class OuraRepository(context: Context) {

    private val appContext = context.applicationContext
    private val tokenStore = TokenStore(appContext)
    private val cache = WidgetCache(appContext)

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    private val api: OuraApi by lazy {
        Retrofit.Builder()
            .baseUrl(OuraApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OuraApi::class.java)
    }

    fun cachedSnapshot(): OuraSnapshot? = cache.load()

    fun isConfigured(): Boolean = tokenStore.hasToken()

    suspend fun refresh(): OuraResult {
        val token = tokenStore.getToken() ?: return OuraResult.NotConfigured

        val today = LocalDate.now()
        // Include yesterday too: Oura may not have published "today" yet right after midnight.
        val startDate = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayStr = endDate

        return try {
            val response = api.getDailyActivity(
                bearerToken = "Bearer $token",
                startDate = startDate,
                endDate = endDate
            )

            when {
                response.code() == 401 || response.code() == 403 -> OuraResult.Unauthorized
                !response.isSuccessful -> OuraResult.NetworkError("HTTP ${response.code()}")
                else -> {
                    val entries = response.body()?.data.orEmpty()
                    val entry = entries.firstOrNull { it.day == todayStr } ?: entries.lastOrNull()
                    if (entry == null) {
                        OuraResult.NetworkError("No data yet")
                    } else {
                        val snapshot = OuraSnapshot(
                            steps = entry.steps ?: 0,
                            activeCalories = entry.activeCalories ?: 0,
                            fetchedAtEpochMillis = System.currentTimeMillis()
                        )
                        cache.save(snapshot)
                        OuraResult.Success(snapshot)
                    }
                }
            }
        } catch (e: IOException) {
            OuraResult.NetworkError(e.message)
        }
    }
}
