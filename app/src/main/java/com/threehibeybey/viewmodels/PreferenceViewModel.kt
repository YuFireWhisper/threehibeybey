package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.repositories.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage user preferences.
 */
class PreferenceViewModel : ViewModel() {

    private val preferenceRepository = PreferencesRepository(FirebaseFirestore.getInstance())
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _preferences = MutableStateFlow<Map<String, Any>>(emptyMap())
    val preferences: StateFlow<Map<String, Any>> = _preferences

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadUserPreferences()
    }

    /**
     * Loads user preferences from repository.
     */
    private fun loadUserPreferences() {
        val user = firebaseAuth.currentUser
        user?.let {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val prefs = preferenceRepository.getUserPreferences(it.uid)
                    if (prefs != null) {
                        _preferences.value = prefs
                        _error.value = null
                    } else {
                        _error.value = "無法載入偏好設置"
                    }
                } catch (e: Exception) {
                    _error.value = e.message ?: "發生錯誤"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Updates a preference value.
     * @param key The preference key.
     * @param value The preference value.
     */
    fun updatePreference(key: String, value: Any) {
        val user = firebaseAuth.currentUser
        user?.let {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val result = preferenceRepository.updateSinglePreference(it.uid, key, value)
                    if (result) {
                        val updatedPrefs = _preferences.value.toMutableMap()
                        updatedPrefs[key] = value
                        _preferences.value = updatedPrefs
                        _error.value = null
                    } else {
                        _error.value = "無法更新偏好設置"
                    }
                } catch (e: Exception) {
                    _error.value = e.message ?: "發生錯誤"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Resets all preferences to default values.
     */
    fun resetPreferences() {
        val user = firebaseAuth.currentUser
        user?.let {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val result = preferenceRepository.deleteUserPreferences(it.uid)
                    if (result) {
                        loadUserPreferences() // Reload default preferences
                        _error.value = null
                    } else {
                        _error.value = "無法重置偏好設置"
                    }
                } catch (e: Exception) {
                    _error.value = e.message ?: "發生錯誤"
                } finally {
                    _isLoading.value = false
                }
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