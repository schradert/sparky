package com.sparkysballoons.invx.auth.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import com.sparkysballoons.invx.auth.domain.User
import com.sparkysballoons.invx.core.domain.DomainResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
): ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = repository.getUser()
            _state.update { it.copy(user = user) }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.signIn()
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = if (result.isErr) "Sign in failed" else null
                )
            }
        }
    }
}