package com.sparky.inventory.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sparky.inventory.presentation.viewmodel.ScannerViewModel
import com.sparky.inventory.di.Dependencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onItemFound: (com.sparky.inventory.domain.model.InventoryItem) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = Dependencies.createScannerViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Scanner") }
        )
        
        Box(modifier = Modifier.weight(1f)) {
            when {
                !uiState.hasPermission -> {
                    PermissionRequiredContent(
                        onRequestPermission = { viewModel.requestPermission() }
                    )
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                uiState.foundItem != null -> {
                    ScannedItemContent(
                        item = uiState.foundItem!!,
                        scannedCode = uiState.scannedCode ?: "",
                        onScanAgain = {
                            viewModel.clearResults()
                            viewModel.startScanning()
                        },
                        onViewItem = { onItemFound(uiState.foundItem!!) }
                    )
                }
                
                uiState.isScanning -> {
                    ScanningContent(
                        onStopScanning = { viewModel.stopScanning() }
                    )
                }
                
                else -> {
                    ScannerIdleContent(
                        onStartScanning = { viewModel.startScanning() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "We need camera access to scan barcodes and QR codes",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun ErrorContent(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Scanner Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun ScannedItemContent(
    item: com.sparky.inventory.domain.model.InventoryItem,
    scannedCode: String,
    onScanAgain: () -> Unit,
    onViewItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Item Found!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Scanned: $scannedCode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InventoryItemCard(
            item = item,
            onItemClick = { onViewItem() },
            onQuantityUpdate = { /* Handle quantity update */ }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onViewItem,
                modifier = Modifier.weight(1f)
            ) {
                Text("View Item")
            }
            
            OutlinedButton(
                onClick = onScanAgain,
                modifier = Modifier.weight(1f)
            ) {
                Text("Scan Another")
            }
        }
    }
}

@Composable
fun ScanningContent(
    onStopScanning: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera preview would go here - this is where platform-specific camera UI would be integrated
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera Preview\n(Camera implementation needed)",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Point camera at barcode or QR code",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onStopScanning) {
                    Text("Stop Scanning")
                }
            }
        }
    }
}

@Composable
fun ScannerIdleContent(
    onStartScanning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ready to Scan",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the button below to start scanning barcodes and QR codes",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onStartScanning) {
            Text("Start Scanning")
        }
    }
}