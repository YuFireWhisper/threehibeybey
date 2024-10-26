package com.threehibeybey.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.PreferenceRestaurant
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling preferences and restaurant data in Firestore.
 */
class PreferencesRepository(private val firestore: FirebaseFirestore) {
    private val preferencesCollection = firestore.collection("preferences")
    private val usersCollection = firestore.collection("users")

    /**
     * Retrieves all restaurants from the preferences collection.
     */
    suspend fun getRestaurants(): List<PreferenceRestaurant> {
        return try {
            val snapshot = preferencesCollection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(PreferenceRestaurant::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Updates or adds a restaurant document.
     */
    suspend fun updateRestaurant(restaurant: PreferenceRestaurant): Boolean {
        return try {
            val query = preferencesCollection.whereEqualTo("name", restaurant.name).get().await()
            if (query.documents.isNotEmpty()) {
                val document = query.documents.first()
                document.reference.set(restaurant).await()
            } else {
                preferencesCollection.add(restaurant).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves user preferences from Firestore.
     */
    suspend fun getUserPreferences(userId: String): Map<String, Any>? {
        return try {
            val document = usersCollection
                .document(userId)
                .collection("preferences")
                .document("settings")
                .get()
                .await()

            if (document.exists()) {
                document.data
            } else {
                mapOf(
                    "darkMode" to false,
                    "notifications" to true,
                    "calorieGoal" to 2000
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves user preferences to Firestore.
     */
    suspend fun saveUserPreferences(userId: String, preferences: Map<String, Any>): Boolean {
        return try {
            usersCollection
                .document(userId)
                .collection("preferences")
                .document("settings")
                .set(preferences)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Updates a single preference value.
     */
    suspend fun updateSinglePreference(userId: String, key: String, value: Any): Boolean {
        return try {
            usersCollection
                .document(userId)
                .collection("preferences")
                .document("settings")
                .update(key, value)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes all preferences for a user.
     */
    suspend fun deleteUserPreferences(userId: String): Boolean {
        return try {
            usersCollection
                .document(userId)
                .collection("preferences")
                .document("settings")
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}