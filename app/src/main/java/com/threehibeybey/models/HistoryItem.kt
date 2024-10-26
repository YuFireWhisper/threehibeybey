package com.threehibeybey.models

/**
 * Data class representing a history record.
 *
 * @param id The unique identifier of the history item.
 * @param restaurantName The name of the restaurant.
 * @param items The list of menu items selected.
 * @param totalCalories The total calories of the items.
 * @param totalPrice The total price of the items.
 * @param timestamp The time when the items were selected.
 */
data class HistoryItem(
    var id: String = "",
    val restaurantName: String = "",
    val items: List<MenuItem> = emptyList(),
    val totalCalories: Double = 0.0,
    val totalPrice: Int = 0,
    val timestamp: Long = 0L
)
