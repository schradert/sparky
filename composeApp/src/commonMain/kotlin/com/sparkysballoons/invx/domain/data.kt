package com.sparkysballoons.invx.domain

import kotlinx.coroutines.flow.Flow

interface InventoryApi {
    suspend fun getProducts(): DomainResult<List<Product>>
    suspend fun getProductById(id: String): DomainResult<Product>
    // suspend fun searchProducts(query: String): DomainResult<List<Product>>
    // suspend fun addProduct(product: Product): DomainResult<Unit>
    // suspend fun updateProductQuantity(id: String, quantity: Int): DomainResult<Unit>
}

interface InventoryStorage {
    suspend fun saveProducts(new: List<Product>): Unit
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    // suspend fun searchProducts(query: String): DomainResult<List<Product>>
    // suspend fun addProduct(product: Product): DomainResult<Unit>
    // suspend fun updateProductQuantity(id: String, quantity: Int): DomainResult<Unit>
}

interface InventoryRepository {
    suspend fun refresh(): Unit
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    // suspend fun searchProducts(query: String): DomainResult<List<Product>>
    // suspend fun addProduct(product: Product): DomainResult<Unit>
    // suspend fun updateProductQuantity(id: String, quantity: Int): DomainResult<Unit>
}