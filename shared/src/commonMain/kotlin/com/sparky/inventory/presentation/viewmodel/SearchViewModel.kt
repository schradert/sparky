package com.sparky.inventory.presentation.viewmodel

import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searchResults: List<InventoryItem> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val inventoryRepository: InventoryRepository
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        if (query.length >= 2) {
            searchItems(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }
    
    private fun searchItems(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            val result = inventoryRepository.searchItems(query)
            if (result.isSuccess()) {
                _uiState.value = _uiState.value.copy(
                    searchResults = result.getDataOrNull() ?: emptyList(),
                    isSearching = false
                )
            } else {
                val error = result.getErrorOrNull()
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = error?.message ?: "Search failed"
                )
            }
        }
    }
    
    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}