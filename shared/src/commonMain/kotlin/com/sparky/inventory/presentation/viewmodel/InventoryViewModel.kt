package com.sparky.inventory.presentation.viewmodel

import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val filteredItems: List<InventoryItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()
    
    init {
        loadItems()
    }
    
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                inventoryRepository.getAllItems().collect { items ->
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        filteredItems = if (_uiState.value.searchQuery.isEmpty()) items else filterItems(items, _uiState.value.searchQuery),
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load items"
                )
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val result = inventoryRepository.syncWithRemote()
            if (result.isError()) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = result.getErrorOrNull()?.message ?: "Failed to refresh items"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredItems = if (query.isEmpty()) currentState.items else filterItems(currentState.items, query)
        )
    }
    
    fun updateQuantity(sku: String, newQuantity: Int) {
        viewModelScope.launch {
            val result = inventoryRepository.updateQuantity(sku, newQuantity)
            if (result.isError()) {
                val error = result.getErrorOrNull()
                _uiState.value = _uiState.value.copy(
                    error = error?.message ?: "Failed to update quantity"
                )
            }
            // Items will be updated via the flow if successful
        }
    }
    
    private fun filterItems(items: List<InventoryItem>, query: String): List<InventoryItem> {
        val lowerQuery = query.lowercase()
        return items.filter { item ->
            item.name.lowercase().contains(lowerQuery) ||
            item.description.lowercase().contains(lowerQuery) ||
            item.supplier.lowercase().contains(lowerQuery) ||
            item.sku.lowercase().contains(lowerQuery) ||
            item.keywords.lowercase().contains(lowerQuery)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}