package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.models.MenuItem
import com.threehibeybey.repositories.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling personal-related operations.
 *
 * @param historyRepository The repository for history data.
 */
class PersonalViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _history: MutableStateFlow<List<HistoryItem>> = MutableStateFlow(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    private val _historyState: MutableStateFlow<HistoryState> = MutableStateFlow(HistoryState.Idle)
    val historyState: StateFlow<HistoryState> = _historyState

    /**
     * Adds a single food item to the history.
     *
     * @param foodItem The food item to add.
     */
    fun addToHistory(foodItem: MenuItem) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.addFoodItem(foodItem)
            if (result) {
                _historyState.value = HistoryState.Success
            } else {
                _historyState.value = HistoryState.Error("無法新增至歷史紀錄。")
            }
        }
    }

    /**
     * Saves the selected foods to history.
     */
    fun saveSelectedFoods(
        selectedFoods: List<MenuItem>,
        restaurantName: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.saveHistory(
                foods = selectedFoods,
                restaurantName = restaurantName,
                timestamp = timestamp
            )
            if (result) {
                _historyState.value = HistoryState.Success
                // Refresh the history after saving
                getHistory()
            } else {
                _historyState.value = HistoryState.Error("無法保存歷史紀錄。")
            }
        }
    }

    /**
     * Retrieves the user's history.
     */
    fun getHistory() {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val historyItems = historyRepository.getHistory()
            _history.value = historyItems
            _historyState.value = HistoryState.Success
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
