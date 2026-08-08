package dev.srini.ourawidget

import android.app.Application
import dev.srini.ourawidget.work.WidgetUpdateScheduler

class OuraWidgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WidgetUpdateScheduler.ensurePeriodicRefresh(this)
    }
}
