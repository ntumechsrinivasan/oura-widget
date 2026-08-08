package dev.srini.ourawidget.data

import kotlinx.serialization.Serializable

@Serializable
data class DailyActivityResponse(
    val data: List<DailyActivityEntry> = emptyList()
)

@Serializable
data class DailyActivityEntry(
    val day: String,
    val steps: Int? = null,
    @kotlinx.serialization.SerialName("active_calories")
    val activeCalories: Int? = null
)

data class OuraSnapshot(
    val steps: Int,
    val activeCalories: Int,
    val fetchedAtEpochMillis: Long
)

sealed interface OuraResult {
    data class Success(val snapshot: OuraSnapshot) : OuraResult
    data object NotConfigured : OuraResult
    data object Unauthorized : OuraResult
    data class NetworkError(val message: String?) : OuraResult
}
