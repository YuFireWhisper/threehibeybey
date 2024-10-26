package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.repositories.PreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to interact between UI and PreferenceRepository.
 */
class PreferenceViewModel : ViewModel() {

    private val preferenceRepository = PreferenceRepository(FirebaseFirestore.getInstance())
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _preferences: MutableStateFlow<Map<String, Any>> = MutableStateFlow(emptyMap())
    val preferences: StateFlow<Map<String, Any>> = _preferences

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
                val prefs = preferenceRepository.getUserPreferences(it.uid)
                if (prefs != null) {
                    _preferences.value = prefs
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
        val updatedPrefs = _preferences.value.toMutableMap()
        updatedPrefs[key] = value
        _preferences.value = updatedPrefs

        // Save to repository
        val user = firebaseAuth.currentUser
        user?.let {
            viewModelScope.launch {
                preferenceRepository.saveUserPreferences(it.uid, updatedPrefs)
            }
        }
    }
}
