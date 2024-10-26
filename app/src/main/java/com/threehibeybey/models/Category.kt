package com.threehibeybey.models

/**
 * Data class representing a category, containing multiple menu items.
 */
data class Category(
    val name: String = "",
    val items: List<MenuItem> = emptyList()
)