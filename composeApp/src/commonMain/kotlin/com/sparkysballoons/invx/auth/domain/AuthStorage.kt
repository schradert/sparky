package com.sparkysballoons.invx.auth.domain

interface AuthStorage {
    suspend fun saveToken(token: TokenResponse)
    suspend fun getToken(): TokenResponse?
    suspend fun clearToken(): Unit
}
