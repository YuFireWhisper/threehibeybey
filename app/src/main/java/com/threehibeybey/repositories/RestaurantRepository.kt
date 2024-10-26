package com.threehibeybey.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.MenuItem
import com.threehibeybey.models.PreferenceRestaurant
import com.threehibeybey.models.SchoolCanteen

/**
 * Repository for handling restaurant-related data operations.
 */
class RestaurantRepository {
    private val preferencesRepository = PreferencesRepository(FirebaseFirestore.getInstance())

    /**
     * Loads all school canteens from Firestore.
     */
    suspend fun loadSchoolCanteens(): List<SchoolCanteen> {
        val preferenceRestaurants = preferencesRepository.getRestaurants()
        return preferenceRestaurants.map { restaurant -> restaurant.toSchoolCanteen() }
    }

    /**
     * Adds an item to FamilyMart's menu.
     */
    suspend fun addFamilyMartItem(menuItem: MenuItem): Boolean {
        val restaurants = preferencesRepository.getRestaurants()
        val yiYuan = restaurants.find { restaurant -> restaurant.name == "宜園餐廳" } ?: return false

        // Convert the existing data and add the new item
        val updatedItems = yiYuan.items.toMutableList()
        val familyMartMap = updatedItems.find { item ->
            (item as? Map<*, *>)?.get("name") == "全家便利商店"
        }

        if (familyMartMap == null) {
            // Add FamilyMart if it doesn't exist
            updatedItems.add(
                mapOf(
                    "name" to "全家便利商店",
                    "items" to listOf(
                        mapOf(
                            "name" to "分類",
                            "items" to listOf(
                                mapOf(
                                    "name" to menuItem.name,
                                    "price" to menuItem.price,
                                    "calories" to menuItem.calories
                                )
                            )
                        )
                    )
                )
            )
        } else {
            // Update existing FamilyMart
            val familyMartIndex = updatedItems.indexOf(familyMartMap)
            val existingItemsList = (familyMartMap as Map<*, *>)["items"] as? List<*> ?: emptyList<Any>()
            val updatedFamilyMart = mapOf(
                "name" to "全家便利商店",
                "items" to existingItemsList + mapOf(
                    "name" to "分類",
                    "items" to listOf(
                        mapOf(
                            "name" to menuItem.name,
                            "price" to menuItem.price,
                            "calories" to menuItem.calories
                        )
                    )
                )
            )
            updatedItems[familyMartIndex] = updatedFamilyMart
        }

        return preferencesRepository.updateRestaurant(
            PreferenceRestaurant(
                name = "宜園餐廳",
                items = updatedItems
            )
        )
    }
}