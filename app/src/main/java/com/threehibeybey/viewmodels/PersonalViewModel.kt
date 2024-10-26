package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.models.MenuItem
import com.threehibeybey.repositories.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonalViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _history: MutableStateFlow<List<HistoryItem>> = MutableStateFlow(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    private val _historyState: MutableStateFlow<HistoryState> = MutableStateFlow(HistoryState.Idle)
    val historyState: StateFlow<HistoryState> = _historyState

    // 選擇的歷史項目，用於詳細資訊或編輯
    private val _selectedHistoryItem: MutableStateFlow<HistoryItem?> = MutableStateFlow(null)
    val selectedHistoryItem: StateFlow<HistoryItem?> = _selectedHistoryItem

    fun selectHistoryItem(item: HistoryItem?) {
        _selectedHistoryItem.value = item
    }

    fun addToHistory(foodItem: MenuItem) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.addFoodItem(foodItem)
            if (result) {
                _historyState.value = HistoryState.Success
                // 刷新歷史紀錄
                getHistory()
            } else {
                _historyState.value = HistoryState.Error("無法新增至歷史紀錄。")
            }
        }
    }

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
                // 保存後刷新歷史紀錄
                getHistory()
            } else {
                _historyState.value = HistoryState.Error("無法保存歷史紀錄。")
            }
        }
    }

    fun getHistory() {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            try {
                val historyItems = historyRepository.getHistory()
                _history.value = historyItems
                _historyState.value = HistoryState.Success
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error("無法獲取歷史紀錄。")
            }
        }
    }

    fun deleteHistoryItem(itemId: String) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.deleteHistoryItem(itemId)
            if (result) {
                _historyState.value = HistoryState.Success
                // 刪除後刷新歷史紀錄
                getHistory()
            } else {
                _historyState.value = HistoryState.Error("無法刪除歷史紀錄。")
            }
        }
    }

    fun updateHistoryItem(itemId: String, updatedItem: HistoryItem) {
        _historyState.value = HistoryState.Loading
        viewModelScope.launch {
            val result = historyRepository.updateHistoryItem(itemId, updatedItem)
            if (result) {
                _historyState.value = HistoryState.Success
                // 更新後刷新歷史紀錄
                getHistory()
            } else {
                _historyState.value = HistoryState.Error("無法更新歷史紀錄。")
            }
        }
    }

    fun resetHistoryState() {
        _historyState.value = HistoryState.Idle
    }

    sealed class HistoryState {
        data object Idle : HistoryState()
        data object Loading : HistoryState()
        data object Success : HistoryState()
        data class Error(val message: String) : HistoryState()
    }
}
