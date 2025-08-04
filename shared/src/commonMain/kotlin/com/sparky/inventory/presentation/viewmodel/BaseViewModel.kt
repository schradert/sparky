package com.sparky.inventory.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base ViewModel for Kotlin Multiplatform
 * Provides coroutine scope management
 */
abstract class BaseViewModel {
    
    private val job = SupervisorJob()
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + job)
    
    /**
     * Called when the ViewModel is no longer used and will be destroyed
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}