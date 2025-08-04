package com.sparky.inventory.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.sparky.inventory.presentation.screen.InventoryScreen
import com.sparky.inventory.presentation.screen.ScannerScreen
import com.sparky.inventory.presentation.screen.SearchScreen

sealed class Screen(val title: String, val icon: ImageVector) {
    object Inventory : Screen("Inventory", Icons.Default.Home)
    object Scanner : Screen("Scanner", Icons.Default.Settings)
    object Search : Screen("Search", Icons.Default.Search)
}

val bottomNavItems = listOf(
    Screen.Inventory,
    Screen.Scanner,
    Screen.Search
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Inventory) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            Screen.Inventory -> {
                InventoryScreen(
                    onItemClick = { /* Navigate to item detail */ },
                    onAddItemClick = { /* Navigate to add item */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Screen.Scanner -> {
                ScannerScreen(
                    onItemFound = { /* Navigate to item detail */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Screen.Search -> {
                SearchScreen(
                    onItemClick = { /* Navigate to item detail */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}