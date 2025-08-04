package com.sparky.inventory.data.repository

import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.util.PlatformUtils
import com.sparky.inventory.util.AppResult
import com.sparky.inventory.util.AppException
import com.sparky.inventory.util.ErrorType
import com.sparky.inventory.domain.model.User
import com.sparky.inventory.domain.repository.AuthRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

expect class LocalStorage {
    suspend fun saveUser(user: User)
    suspend fun getUser(): User?
    suspend fun clearUser()
}

class AuthRepositoryImpl(
    private val googleSheetsApi: GoogleSheetsApi,
    private val localStorage: LocalStorage
) : AuthRepository {
    
    private val _isLoggedIn = MutableStateFlow(false)
    
    init {
        // Check if user is already logged in on init
        GlobalScope.launch {
            checkAuthStatus()
        }
    }
    
    private suspend fun checkAuthStatus() {
        try {
            val user = localStorage.getUser()
            
            if (user != null) {
                if (user.isSessionValid()) {
                    _isLoggedIn.value = true
                } else {
                    // Session expired, logout user
                    logout()
                }
            } else {
                _isLoggedIn.value = false
            }
        } catch (e: Exception) {
            // Handle error, user not logged in
            _isLoggedIn.value = false
        }
    }
    
    override suspend fun login(email: String): AppResult<User> {
        return try {
            if (email.isBlank()) {
                return AppResult.error("Email cannot be blank", ErrorType.VALIDATION)
            }
            
            // Check if email is approved
            val emailCheckResult = googleSheetsApi.checkApprovedEmail(email)
            if (emailCheckResult.isError()) {
                return AppResult.error(
                    AppException(
                        "Failed to verify email: ${emailCheckResult.getErrorOrNull()?.message}",
                        ErrorType.API_ERROR,
                        emailCheckResult.getErrorOrNull()
                    )
                )
            }
            
            val isApproved = emailCheckResult.getDataOrNull() ?: false
            if (!isApproved) {
                return AppResult.error(
                    "Email '$email' is not authorized. Contact your administrator for access.",
                    ErrorType.AUTHORIZATION
                )
            }
            
            val user = User(
                email = email,
                isApproved = true,
                lastLogin = PlatformUtils.currentTimeMillis()
            )
            
            localStorage.saveUser(user)
            _isLoggedIn.value = true
            
            AppResult.success(user)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Login failed: ${e.message}",
                    ErrorType.UNKNOWN,
                    e
                )
            )
        }
    }
    
    override suspend fun logout() {
        localStorage.clearUser()
        _isLoggedIn.value = false
    }
    
    override suspend fun getCurrentUser(): User? {
        return localStorage.getUser()
    }
    
    override fun isLoggedIn(): Flow<Boolean> {
        return _isLoggedIn.asStateFlow()
    }
}