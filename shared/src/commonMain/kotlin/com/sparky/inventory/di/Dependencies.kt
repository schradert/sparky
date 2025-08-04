package com.sparky.inventory.di

import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.data.repository.AuthRepositoryImpl
import com.sparky.inventory.data.repository.InventoryRepositoryImpl
import com.sparky.inventory.domain.repository.AuthRepository
import com.sparky.inventory.domain.repository.InventoryRepository
import com.sparky.inventory.presentation.viewmodel.AuthViewModel
import com.sparky.inventory.presentation.viewmodel.InventoryViewModel
import com.sparky.inventory.presentation.viewmodel.ScannerViewModel
import com.sparky.inventory.presentation.viewmodel.SearchViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Dependencies : KoinComponent {
    val googleSheetsApi: GoogleSheetsApi by inject()
    val authRepository: AuthRepository by inject()
    val inventoryRepository: InventoryRepository by inject()
    
    fun createAuthViewModel(): AuthViewModel = AuthViewModel(authRepository)
    fun createInventoryViewModel(): InventoryViewModel = InventoryViewModel(inventoryRepository)
    // Note: CameraController needs platform-specific implementation
    // For now, using a placeholder that will be implemented per platform
    fun createScannerViewModel(): ScannerViewModel {
        // This will need to be injected from platform modules
        val cameraController = try {
            inject<com.sparky.inventory.domain.camera.CameraController>().value
        } catch (e: Exception) {
            // Fallback for compilation - will need platform implementation
            object : com.sparky.inventory.domain.camera.CameraController {
                override suspend fun hasPermission(): Boolean = false
                override suspend fun requestPermission(): Boolean = false
                override suspend fun startScanning(): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.emptyFlow()
                override suspend fun stopScanning() {}
                override suspend fun capturePhoto(filePath: String): Result<String> = Result.failure(Exception("Camera not implemented"))
            }
        }
        return ScannerViewModel(cameraController, inventoryRepository)
    }
    fun createSearchViewModel(): SearchViewModel = SearchViewModel(inventoryRepository)
}