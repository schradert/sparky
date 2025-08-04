package com.sparky.inventory.domain.camera

import kotlinx.coroutines.flow.Flow

interface CameraController {
    suspend fun startScanning(): Flow<String>
    suspend fun stopScanning()
    suspend fun capturePhoto(filePath: String): Result<String>
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}