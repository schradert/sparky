package com.sparky.inventory.di

import com.sparky.inventory.config.ApiKeyManager
import com.sparky.inventory.data.repository.LocalStorage
import com.sparky.inventory.domain.camera.CameraController
import com.sparky.inventory.domain.camera.IOSCameraController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::IOSCameraController) bind CameraController::class
    singleOf(::LocalStorage)
}

actual fun createApiKeyManager(): ApiKeyManager = ApiKeyManager()