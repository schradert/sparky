package com.sparkysballoons.invx.auth.data

import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.toResultOr
import com.sparkysballoons.invx.auth.domain.AuthApi
import com.sparkysballoons.invx.auth.domain.AuthStorage
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import com.sparkysballoons.invx.auth.domain.TokenResponse
import com.sparkysballoons.invx.core.domain.DomainResult
import com.sparkysballoons.invx.core.domain.ApiError

class BasicAuthRepository(
    private val api: AuthApi,
    private val storage: AuthStorage,
) : AuthRepository {
    override suspend fun getStoredToken(): TokenResponse? = storage.getToken()
    override suspend fun signIn(): DomainResult<GoogleAccount> = api.signIn()

    override suspend fun signOut(): Unit {
        api.signOut()
        storage.clearToken()
    }

    override suspend fun refreshToken(): DomainResult<TokenResponse> = coroutineBinding {
        getStoredToken()
            .toResultOr { ApiError("No token found in local storage") }
            .bind()
            .refreshToken
            .toResultOr { ApiError("No refresh token") }
            .bind()
            .let { api.refreshAccessToken(it) }
            .bind()
            .also { storage.saveToken(it) }
    }
}
