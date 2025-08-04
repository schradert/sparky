package com.sparky.inventory.presentation.viewmodel

import com.sparky.inventory.domain.model.User
import com.sparky.inventory.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isLoggedIn,
                    user = if (isLoggedIn) authRepository.getCurrentUser() else null
                )
            }
        }
    }
    
    fun login(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.login(email)
            if (result.isSuccess()) {
                val user = result.getDataOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = user,
                    error = null
                )
            } else {
                val error = result.getErrorOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error?.message ?: "Login failed"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}