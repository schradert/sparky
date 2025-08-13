package com.sparkysballoons.invx.inventory.data

import com.sparkysballoons.invx.domain.Product
import com.sparkysballoons.invx.inventory.domain.InventoryStorage
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryInventoryStorage : InventoryStorage {
    private val products = MutableStateFlow(emptyList<Product>())

    override suspend fun saveProducts(new: List<Product>) { products.value = new }
    override fun getProducts(): Flow<List<Product>> = products
    override fun getProductById(id: String): Flow<Product?> = products.value.map { it.find { it.uniqueIdSku == id } }
}
