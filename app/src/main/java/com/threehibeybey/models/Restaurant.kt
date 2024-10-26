package com.threehibeybey.models

/**
 * Data class representing a restaurant, containing multiple categories.
 */
data class Restaurant(
    val name: String = "",
    val items: List<Category> = emptyList()
)
