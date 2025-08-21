package com.sparkysballoons.invx.core.di

import com.sparkysballoons.invx.auth.di.AuthModule
import com.sparkysballoons.invx.inventory.di.InventoryModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            AuthModule().module,
            InventoryModule().module
        )
    }
}