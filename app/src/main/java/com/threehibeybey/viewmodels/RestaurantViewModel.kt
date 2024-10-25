package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.utils.JsonLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling restaurant-related operations.
 */
class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {

    private val _canteens: MutableStateFlow<List<SchoolCanteen>> = MutableStateFlow(emptyList())
    val canteens: StateFlow<List<SchoolCanteen>> = _canteens

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    /**
     * Loads the school canteens from the JSON file.
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
     * Adds "全家便利商店" dynamically if "至善餐廳" exists.
     */
    fun augmentCanteens() {
        val updatedCanteens = _canteens.value.map { canteen ->
            if (canteen.name == "至善餐廳") {
                val familyMart = SchoolCanteen(
                    name = "全家便利商店",
                    items = emptyList()
                )
                canteen.copy(items = canteen.items + familyMart)
            } else {
                canteen
            }
        }
        _canteens.value = updatedCanteens
    }
}
