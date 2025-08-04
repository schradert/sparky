package com.sparky.inventory.presentation.viewmodel

import com.sparky.inventory.domain.camera.CameraController
import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isScanning: Boolean = false,
    val scannedCode: String? = null,
    val foundItem: InventoryItem? = null,
    val hasPermission: Boolean = false,
    val error: String? = null
)

class ScannerViewModel(
    private val cameraController: CameraController,
    private val inventoryRepository: InventoryRepository
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    init {
        checkPermission()
    }
    
    fun checkPermission() {
        viewModelScope.launch {
            val hasPermission = cameraController.hasPermission()
            _uiState.value = _uiState.value.copy(hasPermission = hasPermission)
        }
    }
    
    fun requestPermission() {
        viewModelScope.launch {
            val granted = cameraController.requestPermission()
            _uiState.value = _uiState.value.copy(hasPermission = granted)
        }
    }
    
    fun startScanning() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            
            try {
                cameraController.startScanning().collect { scannedCode ->
                    _uiState.value = _uiState.value.copy(scannedCode = scannedCode)
                    lookupItem(scannedCode)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Scanning failed"
                )
            }
        }
    }
    
    fun stopScanning() {
        viewModelScope.launch {
            cameraController.stopScanning()
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }
    
    private suspend fun lookupItem(scannedCode: String) {
        // Try to find item by SKU first
        var item = inventoryRepository.getItemBySku(scannedCode)
        
        // If not found, search by barcode/code
        if (item == null) {
            val searchResult = inventoryRepository.searchItems(scannedCode)
            if (searchResult.isSuccess()) {
                val results = searchResult.getDataOrNull() ?: emptyList()
                item = results.firstOrNull()
            }
        }
        
        _uiState.value = _uiState.value.copy(foundItem = item)
    }
    
    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            scannedCode = null,
            foundItem = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}