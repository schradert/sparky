package com.sparky.inventory.di

import com.sparky.inventory.config.ApiKeyManager
import com.sparky.inventory.data.repository.LocalStorage
import com.sparky.inventory.domain.camera.AndroidCameraController
import com.sparky.inventory.domain.camera.CameraController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::AndroidCameraController) bind CameraController::class
    single { LocalStorage(androidContext()) }
}

actual fun createApiKeyManager(): ApiKeyManager = ApiKeyManager()