package com.threehibeybey.models

/**
 * Data class representing a category within a restaurant.
 */
data class Category(
    val name: String = "",
    val items: List<FoodItem> = emptyList()
)
