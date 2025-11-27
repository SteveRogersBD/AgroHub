package com.example.agrohub.models

/**
 * Data model representing a product in the marketplace
 */
data class Product(
    val id: String,
    val title: String,
    val price: String,
    val location: String,
    val category: String,
    val imageUrl: Int,
    val sellerName: String,
    val sellerAvatar: Int,
    val description: String,
    val sellerPhone: String
)
