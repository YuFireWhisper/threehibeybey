package com.threehibeybey.models

/**
 * Data class representing a restaurant document in Firestore.
 */
data class PreferenceRestaurant(
    val name: String = "",
    val items: List<Any> = emptyList()
) {
    /**
     * Converts Firestore data to SchoolCanteen model.
     */
    fun toSchoolCanteen(): SchoolCanteen {
        return SchoolCanteen(
            name = name,
            items = items.mapNotNull { convertToRestaurant(it) }
        )
    }

    private fun convertToRestaurant(item: Any): Restaurant? {
        if (item !is Map<*, *>) return null
        val restaurantName = item["name"] as? String ?: return null
        val restaurantItems = (item["items"] as? List<*>)?.mapNotNull { convertToCategory(it) } ?: emptyList()
        return Restaurant(name = restaurantName, items = restaurantItems)
    }

    private fun convertToCategory(category: Any?): Category? {
        if (category !is Map<*, *>) return null
        val categoryName = category["name"] as? String ?: return null
        val categoryItems = (category["items"] as? List<*>)?.mapNotNull { convertToMenuItem(it) } ?: emptyList()
        return Category(name = categoryName, items = categoryItems)
    }

    private fun convertToMenuItem(menuItem: Any?): MenuItem? {
        if (menuItem !is Map<*, *>) return null
        val name = menuItem["name"] as? String ?: return null
        val price = (menuItem["price"] as? Number)?.toInt() ?: 0
        val calories = (menuItem["calories"] as? Number)?.toDouble() ?: 0.0
        return MenuItem(name = name, price = price, calories = calories)
    }
}