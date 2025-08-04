package com.sparky.inventory.di

import com.sparky.inventory.config.ApiKeyManager
import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.data.repository.AuthRepositoryImpl
import com.sparky.inventory.data.repository.InventoryRepositoryImpl
import com.sparky.inventory.domain.repository.AuthRepository
import com.sparky.inventory.domain.repository.InventoryRepository
import com.sparky.inventory.presentation.viewmodel.AuthViewModel
import com.sparky.inventory.presentation.viewmodel.InventoryViewModel
import com.sparky.inventory.presentation.viewmodel.ScannerViewModel
import com.sparky.inventory.presentation.viewmodel.SearchViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Configuration - Secure API key management
    single<ApiKeyManager> { createApiKeyManager() }
    
    // Network - Inject ApiKeyManager for secure configuration
    single { GoogleSheetsApi(get()) }
    
    // Repositories
    singleOf(::InventoryRepositoryImpl) bind InventoryRepository::class
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    
    // ViewModels  
    single { AuthViewModel(get()) }
    single { InventoryViewModel(get()) }
    single { ScannerViewModel(get(), get()) }
    single { SearchViewModel(get()) }
}

expect fun createApiKeyManager(): ApiKeyManager

fun initKoin() {
    startKoin {
        modules(appModule, platformModule)
    }
}

expect val platformModule: org.koin.core.module.Module