package com.threehibeybey.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.Category
import com.threehibeybey.models.MenuItem
import com.threehibeybey.models.Restaurant
import com.threehibeybey.models.Subcategory
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.utils.JsonLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel handling operations related to restaurants.
 *
 * @param restaurantRepository The repository for restaurant data.
 */
class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {

    private val _restaurants: MutableStateFlow<List<Restaurant>> = MutableStateFlow(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    /**
     * Loads restaurants from a JSON file and adds "全家便利商店".
     *
     * @param jsonLoader The utility to load JSON.
     * @param context The application context.
     */
    fun loadRestaurants(jsonLoader: JsonLoader, context: Context) {
        viewModelScope.launch {
            try {
                val loadedRestaurants = restaurantRepository.loadRestaurants(jsonLoader, context)
                _restaurants.value = loadedRestaurants

                // Add FamilyMart after loading
                addFamilyMart()
            } catch (e: Exception) {
                _error.value = "無法載入餐廳資料。"
            }
        }
    }

    /**
     * Adds "全家便利商店" to the list of restaurants.
     */
    private fun addFamilyMart() {
        val familyMart = Restaurant(
            name = "全家便利商店",
            items = mutableListOf()
        )
        _restaurants.value += familyMart
    }

    /**
     * Adds a new menu item to "全家便利商店".
     *
     * @param menuItem The menu item to add.
     */
    fun addFamilyMartFoodItem(menuItem: MenuItem) {
        val updatedRestaurants = _restaurants.value.map { restaurant ->
            if (restaurant.name == "全家便利商店") {
                // Check if "自定義品項" category exists
                val existingCategory = restaurant.items.find { it.name == "自定義品項" }
                val updatedItems: List<Category> = if (existingCategory != null) {
                    // Update existing category
                    val updatedCategory = existingCategory.copy(
                        items = existingCategory.items.map { subcategory ->
                            if (subcategory.name == "自定義子分類") {
                                subcategory.copy(items = subcategory.items + menuItem)
                            } else {
                                subcategory
                            }
                        }
                    )
                    restaurant.items.map {
                        if (it.name == "自定義品項") updatedCategory else it
                    }
                } else {
                    // Add new category and subcategory
                    restaurant.items + Category(
                        name = "自定義品項",
                        items = listOf(
                            Subcategory(
                                name = "自定義子分類",
                                items = listOf(menuItem)
                            )
                        )
                    )
                }
                restaurant.copy(items = updatedItems)
            } else {
                restaurant
            }
        }
        _restaurants.value = updatedRestaurants
    }
}
