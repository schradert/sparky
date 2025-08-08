package com.sparkysballoons.invx

import android.app.Application
import com.sparkysballoons.invx.initKoin

class InventoryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}