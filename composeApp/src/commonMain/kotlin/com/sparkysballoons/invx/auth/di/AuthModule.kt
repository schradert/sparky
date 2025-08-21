package com.sparkysballoons.invx.auth.di

import com.sparkysballoons.invx.auth.data.BasicAuthApi
import com.sparkysballoons.invx.auth.data.BasicAuthRepository
import com.sparkysballoons.invx.auth.data.LocalAuthStorage
import com.sparkysballoons.invx.auth.domain.AuthApi
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.domain.AuthStorage
import com.sparkysballoons.invx.auth.view.AuthViewModel
import com.sparkysballoons.invx.core.di.MyAuthHttpClient
import io.ktor.client.HttpClient
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
class AuthModule {

    @Single
    @MyAuthApi
    fun authApi(@MyAuthHttpClient client: HttpClient): AuthApi = BasicAuthApi(client)

    @Single
    @MyAuthStorage
    fun authStorage(context: android.content.Context): AuthStorage = LocalAuthStorage(context)

    @Single
    @MyAuthApiStorage
    fun authRepository(api: AuthApi, storage: AuthStorage) = BasicAuthRepository(api, storage)

    @KoinViewModel
    fun authViewModel(authRepository: AuthRepository) = AuthViewModel(authRepository)
}

@Named
annotation class MyAuthApi

@Named
annotation class MyAuthStorage

@Named
annotation class MyAuthRepository