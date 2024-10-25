package com.threehibeybey.viewmodels

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
 * ViewModel 處理與餐廳相關的操作。
 */
class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {

    private val _restaurants: MutableStateFlow<List<Restaurant>> = MutableStateFlow(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    /**
     * 從 JSON 文件加載餐廳資料，並在成功後添加全家便利商店。
     */
    fun loadRestaurants(jsonLoader: JsonLoader, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val loadedRestaurants = restaurantRepository.loadRestaurants(jsonLoader, context)
                _restaurants.value = loadedRestaurants

                // 在成功載入後添加全家便利商店
                addFamilyMart()
            } catch (e: Exception) {
                _error.value = "無法載入餐廳資料。"
            }
        }
    }

    /**
     * 添加 "全家便利商店" 至餐廳列表中。
     */
    private fun addFamilyMart() {
        val familyMart = Restaurant(
            name = "全家便利商店",
            items = emptyList() // 初始為空，允許用戶自行添加分類和菜單項目
        )
        _restaurants.value = _restaurants.value + familyMart
    }

    /**
     * 向 "全家便利商店" 添加新的菜單項目。
     */
    fun addFamilyMartFoodItem(menuItem: MenuItem) {
        val updatedRestaurants = _restaurants.value.map { restaurant ->
            if (restaurant.name == "全家便利商店") {
                val updatedItems = restaurant.items + Category(
                    name = "自定義品項",
                    items = listOf(
                        Subcategory(
                            name = "自定義子分類",
                            items = listOf(menuItem)
                        )
                    )
                )
                restaurant.copy(items = updatedItems)
            } else {
                restaurant
            }
        }
        _restaurants.value = updatedRestaurants
    }
}
