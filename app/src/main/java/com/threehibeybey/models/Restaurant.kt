package com.threehibeybey.models

/**
 * Data class representing a restaurant within a school canteen.
 */
data class Restaurant(
    val name: String = "",
    val items: List<Category> = emptyList()
)
