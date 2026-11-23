package com.omu.japan

import android.app.Application
import com.omu.japan.data.AppContainer

class OmuApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
