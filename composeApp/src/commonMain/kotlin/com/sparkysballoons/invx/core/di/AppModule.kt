package com.sparkysballoons.invx.core.di

import com.sparkysballoons.invx.core.data.HttpClientEngineFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class AppModule {

    @Single
    @AuthHttpClient
    fun authHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        // ...
    }

    @Single
    @NoAuthHttpClient
    fun noAuthHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        // ...
    }

    @Factory
    fun httpClientEngine(): HttpClientEngine = HttpClientEngineFactory().getHttpEngine()
}

@Named
annotation class AuthHttpClient

@Named
annotation class NoAuthHttpClient