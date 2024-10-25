package com.threehibeybey.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.FoodItem
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling history-related data operations.
 */
class HistoryRepository(private val firestore: FirebaseFirestore) {

    private val historyCollection = firestore.collection("history")

    /**
     * Adds a food item to the user's history in Firebase.
     */
    suspend fun addFoodItem(foodItem: FoodItem): Boolean {
        return try {
            val user = firestore.collection("users").document(firestore.auth.currentUser?.uid ?: "")
            historyCollection.document(user.id).collection("foodItems").add(foodItem).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves the user's food history from Firebase.
     */
    suspend fun getFoodHistory(): List<FoodItem> {
        return try {
            val user = firestore.collection("users").document(firestore.auth.currentUser?.uid ?: "")
            val snapshot = historyCollection.document(user.id).collection("foodItems").get().await()
            snapshot.documents.mapNotNull { it.toObject(FoodItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
