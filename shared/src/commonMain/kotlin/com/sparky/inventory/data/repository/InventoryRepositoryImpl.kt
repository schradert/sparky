package com.sparky.inventory.data.repository

import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.util.PlatformUtils
import com.sparky.inventory.util.AppResult
import com.sparky.inventory.util.AppException
import com.sparky.inventory.util.ErrorType
import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class InventoryRepositoryImpl(
    private val googleSheetsApi: GoogleSheetsApi
) : InventoryRepository {
    
    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    
    override suspend fun getAllItems(): Flow<List<InventoryItem>> {
        syncWithRemote()
        return _items.asStateFlow()
    }
    
    override suspend fun getItemById(id: String): InventoryItem? {
        return _items.value.find { it.id == id }
    }
    
    override suspend fun getItemBySku(sku: String): InventoryItem? {
        return _items.value.find { it.sku == sku }
    }
    
    override suspend fun searchItems(query: String): AppResult<List<InventoryItem>> {
        return googleSheetsApi.searchItems(query)
    }
    
    override suspend fun addItem(item: InventoryItem): AppResult<Unit> {
        val newItem = item.copy(
            id = if (item.id.isEmpty()) generateId() else item.id,
            lastUpdated = PlatformUtils.currentTimeMillis()
        )
        
        return googleSheetsApi.addNewItem(newItem).also { result ->
            if (result.isSuccess()) {
                _items.value = _items.value + newItem
            }
        }
    }
    
    override suspend fun updateQuantity(sku: String, newQuantity: Int): AppResult<Unit> {
        return googleSheetsApi.updateQuantity(sku, newQuantity).also { result ->
            if (result.isSuccess()) {
                // Update local cache
                val updatedItems = _items.value.map { item ->
                    if (item.sku == sku) {
                        item.copy(quantity = newQuantity, lastUpdated = PlatformUtils.currentTimeMillis())
                    } else {
                        item
                    }
                }
                _items.value = updatedItems
            }
        }
    }
    
    override suspend fun syncWithRemote(): AppResult<Unit> {
        return try {
            val result = googleSheetsApi.getInventoryData()
            if (result.isSuccess()) {
                _items.value = result.getDataOrNull() ?: emptyList()
                AppResult.success(Unit)
            } else {
                AppResult.error(
                    AppException(
                        "Failed to sync with remote: ${result.getErrorOrNull()?.message}",
                        ErrorType.API_ERROR,
                        result.getErrorOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Sync failed: ${e.message}",
                    ErrorType.UNKNOWN,
                    e
                )
            )
        }
    }
    
    private fun generateId(): String {
        return "item_${Random.nextLong()}"
    }
}