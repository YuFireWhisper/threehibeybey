package com.threehibeybey.models

/**
 * Data class representing a school canteen.
 */
data class SchoolCanteen(
    val name: String = "",
    val items: List<Any> = emptyList() // Can contain Restaurant or another SchoolCanteen
)
