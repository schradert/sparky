package com.sparky.inventory.domain.repository

import com.sparky.inventory.domain.model.User
import com.sparky.inventory.util.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String): AppResult<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Flow<Boolean>
}