package com.sparkysballons.invx.auth.di

import com.sparkysballoons.invx.core.di.AuthHttpClient
import com.sparkysballoons.invx.auth.data.KtorAuthRepository
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.view.AuthViewModel
import io.ktor.client.HttpClient
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
class AuthModule {

    @Factory(binds = [AuthRepository::class])
    fun authRepository(@AuthHttpClient httpClient: HttpClient) = KtorAuthRepository(httpClient)

    @KoinViewModel
    fun authViewModel(authRepository: AuthRepository) = AuthViewModel(authRepository)
}