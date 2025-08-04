package com.sparky.inventory.domain.repository

import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.util.AppResult
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun getAllItems(): Flow<List<InventoryItem>>
    suspend fun getItemById(id: String): InventoryItem?
    suspend fun getItemBySku(sku: String): InventoryItem?
    suspend fun searchItems(query: String): AppResult<List<InventoryItem>>
    suspend fun addItem(item: InventoryItem): AppResult<Unit>
    suspend fun updateQuantity(sku: String, newQuantity: Int): AppResult<Unit>
    suspend fun syncWithRemote(): AppResult<Unit>
}