package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.repositories.HistoryRepository
import com.threehibeybey.models.FoodItem
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
    fun addToHistory(foodItem: FoodItem) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.addFoodItem(foodItem)
            if (result) {
                _history.value += foodItem
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
