package com.sparkysballoons.invx

import com.sparkysballoons.invx.auth.authModule
import com.sparkysballoons.invx.data.BasicInventoryRepository
import com.sparkysballoons.invx.data.InMemoryInventoryStorage
import com.sparkysballoons.invx.data.SheetsInventoryApi
import com.sparkysballoons.invx.auth.AuthViewModel
import com.sparkysballoons.invx.view.DetailViewModel
import com.sparkysballoons.invx.view.ListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::SheetsInventoryApi)
    singleOf(::BasicInventoryRepository)
    singleOf(::InMemoryInventoryStorage)
}

val viewModelModule = module {
    factory { AuthViewModel(get()) }
    factoryOf(::ListViewModel)
    factoryOf(::DetailViewModel)
}

fun initKoin() = startKoin {
    modules(dataModule, viewModelModule, authModule)
}
