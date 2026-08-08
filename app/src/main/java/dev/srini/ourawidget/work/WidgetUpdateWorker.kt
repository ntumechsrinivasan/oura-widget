package dev.srini.ourawidget.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.srini.ourawidget.OuraWidget
import dev.srini.ourawidget.data.OuraRepository
import dev.srini.ourawidget.data.OuraResult

// Fetches the latest Oura snapshot and re-renders every placed widget instance.
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = OuraRepository(applicationContext)
        val result = repository.refresh()
        OuraWidget().updateAll(applicationContext)

        return when (result) {
            is OuraResult.Success -> Result.success()
            is OuraResult.NotConfigured -> Result.success()
            is OuraResult.Unauthorized -> Result.failure()
            is OuraResult.NetworkError -> Result.retry()
        }
    }
}
