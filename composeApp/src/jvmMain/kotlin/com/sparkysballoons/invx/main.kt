package com.sparkysballoons.invx

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sparkysballoons.invx.core.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            alwaysOnTop = true,
            onCloseRequest = ::exitApplication,
            title = "SparkysBalloonsInventory",
        ) {
            App()
        }
    }
}
