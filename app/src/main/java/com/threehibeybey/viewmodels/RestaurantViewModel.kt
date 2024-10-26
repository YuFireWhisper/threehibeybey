package com.threehibeybey.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.MenuItem
import com.threehibeybey.models.Restaurant
import com.threehibeybey.models.SchoolCanteen
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

    private val _schoolCanteens: MutableStateFlow<List<SchoolCanteen>> = MutableStateFlow(emptyList())
    val schoolCanteens: StateFlow<List<SchoolCanteen>> = _schoolCanteens

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    /**
     * Loads school canteens from a JSON file.
     *
     * @param jsonLoader The utility to load JSON.
     * @param context The application context.
     */
    fun loadSchoolCanteens(jsonLoader: JsonLoader, context: Context) {
        viewModelScope.launch {
            try {
                val loadedCanteens = restaurantRepository.loadSchoolCanteens(jsonLoader, context)
                _schoolCanteens.value = loadedCanteens

                // Add FamilyMart to the appropriate canteen
                addFamilyMart()
            } catch (e: Exception) {
                _error.value = "無法載入學餐資料。"
            }
        }
    }

    /**
     * Adds "全家便利商店" to the "宜園餐廳" in the list of school canteens.
     */
    private fun addFamilyMart() {
        val updatedCanteens = _schoolCanteens.value.map { canteen ->
            if (canteen.name == "宜園餐廳") {
                val updatedRestaurants = canteen.items + Restaurant(
                    name = "全家便利商店",
                    items = listOf() // Empty categories, to be filled when items are added
                )
                canteen.copy(items = updatedRestaurants)
            } else {
                canteen
            }
        }
        _schoolCanteens.value = updatedCanteens
    }

    /**
     * Adds a new menu item to "全家便利商店" under "宜園餐廳".
     *
     * @param menuItem The menu item to add.
     */
    fun addFamilyMartFoodItem(menuItem: MenuItem) {
        val updatedCanteens = _schoolCanteens.value.map { canteen ->
            if (canteen.name == "宜園餐廳") {
                val updatedRestaurants = canteen.items.map { restaurant ->
                    if (restaurant.name == "全家便利商店") {
                        // Find the category "分類"
                        val existingCategory = restaurant.items.find { it.name == "分類" }
                        val updatedItems = if (existingCategory != null) {
                            // Add the new menu item to the existing category
                            val updatedCategory = existingCategory.copy(
                                items = existingCategory.items + menuItem
                            )
                            restaurant.items.map {
                                if (it.name == "分類") updatedCategory else it
                            }
                        } else {
                            // If "分類" category does not exist, add it with the new menu item
                            restaurant.items + com.threehibeybey.models.Category(
                                name = "分類",
                                items = listOf(menuItem)
                            )
                        }
                        restaurant.copy(items = updatedItems)
                    } else {
                        restaurant
                    }
                }
                canteen.copy(items = updatedRestaurants)
            } else {
                canteen
            }
        }
        _schoolCanteens.value = updatedCanteens
    }
}
