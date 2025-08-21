package com.sparkysballoons.invx.inventory.di

import com.sparkysballoons.invx.auth.di.MyAuthRepository
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.inventory.data.BasicInventoryRepository
import com.sparkysballoons.invx.inventory.data.InMemoryInventoryStorage
import com.sparkysballoons.invx.inventory.data.SheetsInventoryApi
import com.sparkysballoons.invx.inventory.domain.InventoryApi
import com.sparkysballoons.invx.inventory.domain.InventoryRepository
import com.sparkysballoons.invx.inventory.domain.InventoryStorage
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class InventoryModule {

    @Single
    @MyInventoryApi
    fun inventoryApi(@MyAuthRepository authRepository: AuthRepository) = SheetsInventoryApi(authRepository)

    @Single
    @MyInventoryStorage
    fun inventoryStorage() = InMemoryInventoryStorage()

    @Single
    @MyInventoryRepository
    fun inventoryRepository(
        @MyInventoryApi api: InventoryApi,
        @MyInventoryStorage storage: InventoryStorage,
    ) = BasicInventoryRepository(api, storage)
}

@Named
annotation class MyInventoryApi

@Named
annotation class MyInventoryStorage

@Named
annotation class MyInventoryRepository