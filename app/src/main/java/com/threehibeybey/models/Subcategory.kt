package com.threehibeybey.models

/**
 * Data class representing a subcategory, containing multiple menu items.
 */
data class Subcategory(
    val name: String = "",
    val items: List<MenuItem> = emptyList()
)
