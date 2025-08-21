package com.sparkysballoons.invx.inventory.data

import com.sparkysballoons.invx.inventory.domain.Product
import com.sparkysballoons.invx.inventory.domain.InventoryApi
import com.sparkysballoons.invx.inventory.domain.InventoryRepository
import com.sparkysballoons.invx.inventory.domain.InventoryStorage
import com.github.michaelbull.result.getOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


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
