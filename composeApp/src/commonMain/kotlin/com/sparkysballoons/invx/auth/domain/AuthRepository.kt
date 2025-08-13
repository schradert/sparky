package com.sparkysballoons.invx.auth.domain

import com.sparkysballoons.invx.core.domain.DomainResult

interface AuthRepository {
    suspend fun signIn(): DomainResult<GoogleAccount>
    suspend fun signOut(): Unit
    suspend fun refreshToken(): DomainResult<TokenResponse>
    suspend fun getStoredToken(): TokenResponse?
    suspend fun getUser(): User?
}
