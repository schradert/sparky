package com.sparkysballoons.invx

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        alwaysOnTop = true,
        onCloseRequest = ::exitApplication,
        title = "SparkysBalloonsInventory",
    ) {
        App()
    }
}