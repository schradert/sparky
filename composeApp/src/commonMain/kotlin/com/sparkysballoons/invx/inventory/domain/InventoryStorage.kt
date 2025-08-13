package com.sparkysballoons.invx.inventory.domain

import kotlinx.coroutines.flow.Flow

interface InventoryStorage {
    suspend fun saveProducts(new: List<Product>): Unit
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    // suspend fun searchProducts(query: String): DomainResult<List<Product>>
    // suspend fun addProduct(product: Product): DomainResult<Unit>
    // suspend fun updateProductQuantity(id: String, quantity: Int): DomainResult<Unit>
}
