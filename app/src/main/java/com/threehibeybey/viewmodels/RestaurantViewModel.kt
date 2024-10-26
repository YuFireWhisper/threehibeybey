package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.MenuItem
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.repositories.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel handling operations related to restaurants.
 */
class RestaurantViewModel(private val restaurantRepository: RestaurantRepository) : ViewModel() {

    private val _schoolCanteens: MutableStateFlow<List<SchoolCanteen>> = MutableStateFlow(emptyList())
    val schoolCanteens: StateFlow<List<SchoolCanteen>> = _schoolCanteens

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error

    init {
        loadSchoolCanteens()
    }

    /**
     * Loads school canteens from Firestore.
     */
    fun loadSchoolCanteens() {
        viewModelScope.launch {
            try {
                val loadedCanteens = restaurantRepository.loadSchoolCanteens()
                _schoolCanteens.value = loadedCanteens
                _error.value = null
            } catch (e: Exception) {
                _error.value = "無法載入學餐資料。"
            }
        }
    }

    /**
     * Adds a new menu item to FamilyMart.
     */
    fun addFamilyMartFoodItem(menuItem: MenuItem) {
        viewModelScope.launch {
            try {
                val success = restaurantRepository.addFamilyMartItem(menuItem)
                if (success) {
                    loadSchoolCanteens()
                } else {
                    _error.value = "無法新增品項。"
                }
            } catch (e: Exception) {
                _error.value = "發生錯誤：${e.message}"
            }
        }
    }

    /**
     * Clears any error state.
     */
    fun clearError() {
        _error.value = null
    }
}