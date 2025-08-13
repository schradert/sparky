package com.sparkysballoons.invx.data

import com.sparkysballoons.invx.domain.Product
import com.sparkysballoons.invx.domain.DomainResult
import com.sparkysballoons.invx.domain.InventoryApi
import com.sparkysballoons.invx.domain.InventoryRepository
import com.sparkysballoons.invx.domain.InventoryStorage
import com.github.michaelbull.result.getOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InMemoryInventoryStorage : InventoryStorage {
    private val products = MutableStateFlow(emptyList<Product>())

    override suspend fun saveProducts(new: List<Product>) { products.value = new }
    override fun getProducts(): Flow<List<Product>> = products
    override fun getProductById(id: String): Flow<Product?> = products.map { it.find { it.uniqueIdSku == id } }
}

class BasicInventoryRepository(
    private val api: InventoryApi,
    private val storage: InventoryStorage,
): InventoryRepository {
    private val scope = CoroutineScope(SupervisorJob())

    init { scope.launch { refresh() } }

    override suspend fun refresh(): Unit = storage.saveProducts(api.getProducts().getOr(emptyList()))
    override fun getProducts(): Flow<List<Product>> = storage.getProducts()
    override fun getProductById(id: String): Flow<Product?> = storage.getProductById(id)
}
