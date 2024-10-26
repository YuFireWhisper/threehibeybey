package com.threehibeybey.models

/**
 * Data class representing a category, containing multiple subcategories.
 */
data class Category(
    val name: String = "",
    val items: List<Subcategory> = emptyList()
)
