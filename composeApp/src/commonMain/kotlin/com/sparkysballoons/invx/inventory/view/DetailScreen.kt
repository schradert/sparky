package com.sparkysballoons.invx.inventory.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkysballoons.invx.core.view.EmptyContent
import com.sparkysballoons.invx.inventory.domain.InventoryRepository
import com.sparkysballoons.invx.inventory.domain.Product
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sparkysballoonsinventory.composeapp.generated.resources.Res
import sparkysballoonsinventory.composeapp.generated.resources.back
import sparkysballoonsinventory.composeapp.generated.resources.label_sku
import sparkysballoonsinventory.composeapp.generated.resources.label_manufacturer_color
import sparkysballoonsinventory.composeapp.generated.resources.label_brand
import sparkysballoonsinventory.composeapp.generated.resources.label_size
import sparkysballoonsinventory.composeapp.generated.resources.label_texture
import sparkysballoonsinventory.composeapp.generated.resources.label_bag_quantity
import sparkysballoonsinventory.composeapp.generated.resources.label_shape
import sparkysballoonsinventory.composeapp.generated.resources.label_distributor
import sparkysballoonsinventory.composeapp.generated.resources.label_occasion
import sparkysballoonsinventory.composeapp.generated.resources.label_quantity

class DetailViewModel(private val repository: InventoryRepository) : ViewModel() {
    fun getProduct(id: String): Flow<Product?> = repository.getProductById(id)
}

@Composable
fun DetailScreen(
    productId: String,
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<DetailViewModel>()
    val product by viewModel.getProduct(productId).collectAsStateWithLifecycle(initialValue = null)

    AnimatedContent(product != null) { productAvailable ->
        if (productAvailable) {
            ProductDetails(product!!, onBackClick = navigateBack)
        } else {
            EmptyContent(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ProductDetails(
    product: Product,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        modifier = modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) { paddingValues ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            SelectionContainer {
                Column(Modifier.padding(12.dp)) {
                    Text(product.uniqueIdSku, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(6.dp))
                    LabeledInfo(stringResource(Res.string.label_sku), product.uniqueIdSku)
                    LabeledInfo(stringResource(Res.string.label_manufacturer_color), product.manufacturerColor)
                    LabeledInfo(stringResource(Res.string.label_brand), product.brand)
                    LabeledInfo(stringResource(Res.string.label_size), product.size.toString())
                    LabeledInfo(stringResource(Res.string.label_texture), product.texture)
                    LabeledInfo(stringResource(Res.string.label_bag_quantity), product.bagQuantity.toString())
                    LabeledInfo(stringResource(Res.string.label_shape), product.shape)
                    LabeledInfo(stringResource(Res.string.label_distributor), product.distributor)
                    LabeledInfo(stringResource(Res.string.label_occasion), product.occasion)
                    LabeledInfo(stringResource(Res.string.label_quantity), product.quantity.toString())
                }
            }
        }
    }
}

@Composable
private fun LabeledInfo(
    label: String,
    data: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(vertical = 4.dp)) {
        Spacer(Modifier.height(6.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$label: ")
                }
                append(data)
            }
        )
    }
}