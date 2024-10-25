package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.Category
import com.threehibeybey.models.CategoryWrapper
import com.threehibeybey.models.MenuItem
import com.threehibeybey.models.MenuItemWrapper
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.utils.JsonLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel處理與餐廳相關的操作。
 */
class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {

    private val _canteens: MutableStateFlow<List<SchoolCanteen>> = MutableStateFlow(emptyList())
    val canteens: StateFlow<List<SchoolCanteen>> = _canteens

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    /**
     * 從JSON文件加載學餐資料。
     */
    fun loadCanteens(jsonLoader: JsonLoader, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val loadedCanteens = restaurantRepository.loadCanteens(jsonLoader, context)
                _canteens.value = loadedCanteens
            } catch (e: Exception) {
                _error.value = "無法載入餐廳資料。"
            }
        }
    }

    /**
     * 添加 "全家便利商店" 至指定的餐廳中。
     */
    fun addFamilyMart() {
        val updatedCanteens = _canteens.value.map { canteen ->
            val updatedRestaurant = canteen.restaurant.copy(
                items = canteen.restaurant.items + CategoryWrapper(
                    category = Category(
                        name = "全家便利商店",
                        items = emptyList() // 初始為空，允許用戶自行添加子分類和菜單項目
                    )
                )
            )
            canteen.copy(restaurant = updatedRestaurant)
        }
        _canteens.value = updatedCanteens
    }

    /**
     * 向 "全家便利商店" 添加新的菜單項目。
     */
    fun addFamilyMartFoodItem(menuItem: MenuItem) {
        val updatedCanteens = _canteens.value.map { canteen ->
            val updatedRestaurant = canteen.restaurant.copy(
                items = canteen.restaurant.items.map { categoryWrapper ->
                    if (categoryWrapper.category.name == "全家便利商店") {
                        categoryWrapper.copy(
                            category = categoryWrapper.category.copy(
                                items = categoryWrapper.category.items.map { subcategoryWrapper ->
                                    subcategoryWrapper.copy(
                                        subcategory = subcategoryWrapper.subcategory.copy(
                                            items = subcategoryWrapper.subcategory.items + MenuItemWrapper(
                                                menu_item = menuItem
                                            )
                                        )
                                    )
                                }
                            )
                        )
                    } else {
                        categoryWrapper
                    }
                }
            )
            canteen.copy(restaurant = updatedRestaurant)
        }
        _canteens.value = updatedCanteens
    }
}
