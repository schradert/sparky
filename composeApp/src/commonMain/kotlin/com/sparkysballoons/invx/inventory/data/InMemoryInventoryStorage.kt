package com.sparkysballoons.invx.inventory.data

import com.sparkysballoons.invx.inventory.domain.Product
import com.sparkysballoons.invx.inventory.domain.InventoryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InMemoryInventoryStorage : InventoryStorage {
    private val products = MutableStateFlow(emptyList<Product>())

    override suspend fun saveProducts(new: List<Product>) { products.value = new }
    override fun getProducts(): Flow<List<Product>> = products
    override fun getProductById(id: String): Flow<Product?> = products.map { productList -> productList.find { it.uniqueIdSku == id } }
}
