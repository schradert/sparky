package com.sparkysballoons.invx.inventory.domain

import kotlinx.serialization.Serializable

@Serializable
private data class ProductType(
    val id: Int,
    val name: String
)

@Serializable
private data class ManufacturerColor(
    val id: Int,
    val name: String
)

@Serializable
private data class Brand(
    val id: Int,
    val name: String
)

@Serializable
private data class Texture(
    val id: Int,
    val name: String
)

@Serializable
private data class Shape(
    val id: Int,
    val name: String
)

@Serializable
private data class Distributor(
    val id: Int,
    val name: String
)

@Serializable
private data class Occasion(
    val id: Int,
    val name: String
)

@Serializable
data class Product(
    val uniqueIdSku: String,
    val productType: String,
    val manufacturerColor: String,
    val brand: String,
    val size: Double,
    val texture: String,
    val bagQuantity: Int,
    val shape: String,
    val distributor: String,
    val occasion: String,
    val quantity: Int
)
