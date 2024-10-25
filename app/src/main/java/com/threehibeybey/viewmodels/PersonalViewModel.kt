package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.FoodItem
import com.threehibeybey.models.MenuItem
import com.threehibeybey.repositories.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling personal-related operations.
 */
class PersonalViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _history: MutableStateFlow<List<FoodItem>> = MutableStateFlow(emptyList())
    val history: StateFlow<List<FoodItem>> = _history

    private val _historyState: MutableStateFlow<HistoryState> = MutableStateFlow(HistoryState.Idle)
    val historyState: StateFlow<HistoryState> = _historyState

    /**
     * Adds a food item to the history.
     */
    fun addToHistory(foodItem: MenuItem) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.addFoodItem(foodItem)
            if (result) {
                // 將 MenuItem 轉換為 FoodItem
                val food = FoodItem(
                    name = foodItem.name,
                    price = foodItem.price,
                    calories = foodItem.calories
                )
                _history.value = _history.value + food
                _historyState.value = HistoryState.Success
            } else {
                _historyState.value = HistoryState.Error("無法新增至歷史紀錄。")
            }
        }
    }

    /**
     * Represents the history state.
     */
    sealed class HistoryState {
        object Idle : HistoryState()
        object Loading : HistoryState()
        object Success : HistoryState()
        data class Error(val message: String) : HistoryState()
    }
}
