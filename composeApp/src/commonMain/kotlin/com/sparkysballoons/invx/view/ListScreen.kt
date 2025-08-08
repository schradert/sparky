package com.sparkysballoons.invx.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkysballoons.invx.domain.InventoryRepository
import com.sparkysballoons.invx.domain.Product
import com.sparkysballoons.invx.view.EmptyContent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel

class ListViewModel(repository: InventoryRepository) : ViewModel() {
    val products = repository.getProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ListScreen(
    navigateToDetails: (productId: Int) -> Unit
) {
    val viewModel = koinViewModel<ListViewModel>()
    val products by viewModel.products.collectAsStateWithLifecycle()

    AnimatedContent(products.isNotEmpty()) { productsAvailable ->
        if (productsAvailable) {
            ProductGrid(
                products = products,
                onProductClick = navigateToDetails,
            )
        } else {
            EmptyContent(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
    ) {
        items(products, key = { it.uniqueIdSku }) { product ->
            ProductFrame(
                product = product,
                onClick = { onProductClick(product.uniqueIdSku) },
            )
        }
    }
}

@Composable
private fun ProductFrame(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(product.uniqueIdSku, style = MaterialTheme.typography.titleMedium)
        Text(product.brand, style = MaterialTheme.typography.bodyMedium)
    }
}