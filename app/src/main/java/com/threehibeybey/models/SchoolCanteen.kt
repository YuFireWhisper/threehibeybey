package com.threehibeybey.models

/**
 * Data class representing the school canteen, containing multiple restaurants.
 */
data class SchoolCanteen(
    val name: String = "",
    val items: List<Restaurant> = emptyList()
)
