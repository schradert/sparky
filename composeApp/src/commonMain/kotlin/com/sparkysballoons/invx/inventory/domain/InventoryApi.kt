package com.sparkysballoons.invx.inventory.domain

import com.sparkysballoons.invx.core.domain.DomainResult

interface InventoryApi {
    suspend fun getProducts(): DomainResult<List<Product>>
    suspend fun getProductById(id: String): DomainResult<Product>
    // suspend fun searchProducts(query: String): DomainResult<List<Product>>
    // suspend fun addProduct(product: Product): DomainResult<Unit>
    // suspend fun updateProductQuantity(id: String, quantity: Int): DomainResult<Unit>
}
