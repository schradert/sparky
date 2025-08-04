package com.sparky.inventory.domain.model

import kotlinx.serialization.Serializable
import com.sparky.inventory.util.PlatformUtils

@Serializable
data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val supplier: String = "",
    val sku: String = "",
    val quantity: Int = 0,
    val imageUrl: String = "",
    val keywords: String = "",
    val lastUpdated: Long = PlatformUtils.currentTimeMillis()
)