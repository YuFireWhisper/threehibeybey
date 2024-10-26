package com.threehibeybey.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for handling user preference data operations with Firebase Firestore.
 */
class PreferenceRepository(private val firestore: FirebaseFirestore) {

    private val preferencesCollection = firestore.collection("preferences")

    /**
     * Fetches user preferences from Firestore.
     * @param userId The ID of the user.
     * @return A map of preferences.
     */
    suspend fun getUserPreferences(userId: String): Map<String, Any>? {
        return try {
            val document = preferencesCollection.document(userId)
                .get(Source.CACHE)
                .await()
            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves user preferences to Firestore.
     * @param userId The ID of the user.
     * @param preferences A map of preferences to save.
     */
    suspend fun saveUserPreferences(userId: String, preferences: Map<String, Any>) {
        try {
            preferencesCollection.document(userId).set(preferences).await()
        } catch (e: Exception) {
            // Handle exception
        }
    }
}
