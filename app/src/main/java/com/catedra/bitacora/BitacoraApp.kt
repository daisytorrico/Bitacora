package com.catedra.bitacora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class BitacoraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("${packageName}_preferences", MODE_PRIVATE)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().userAgentValue = "com.catedra.bitacora"
    }
}