package com.sparky.inventory.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.sparky.inventory.di.initKoin
import com.sparky.inventory.di.Dependencies
import com.sparky.inventory.presentation.navigation.AppNavigation
import com.sparky.inventory.presentation.screen.LoginScreen

@Composable
fun App() {
    LaunchedEffect(Unit) {
        initKoin()
    }
    
    MaterialTheme {
        AppContent()
    }
}

@Composable
fun AppContent() {
    val authViewModel = Dependencies.createAuthViewModel()
    val uiState by authViewModel.uiState.collectAsState()
    
    if (uiState.isLoggedIn) {
        AppNavigation()
    } else {
        LoginScreen(
            onLoginSuccess = { /* Navigation handled by state change */ }
        )
    }
}