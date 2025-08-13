package com.sparkysballoons.invx.auth.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import com.sparkysballoons.invx.core.domain.DomainResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
): ViewModel() {
    private val _state = MutableStateFlow(emptyList<String>())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = repository.getUser()
            _state.update { user }
        }
    }

    suspend fun signIn(): DomainResult<GoogleAccount> = repository.signIn()
}