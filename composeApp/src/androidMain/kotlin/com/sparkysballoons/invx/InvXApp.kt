package com.sparkysballoons.invx

import android.app.Application
import com.sparkysballoons.invx.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class InvXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@InvXApp)
        }
    }
}