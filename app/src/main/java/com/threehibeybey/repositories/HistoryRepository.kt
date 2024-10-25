package com.threehibeybey.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.FoodItem
import com.threehibeybey.models.MenuItem
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling history-related data operations.
 */
class HistoryRepository(private val firestore: FirebaseFirestore, private val firebaseAuth: FirebaseAuth) {

    private val historyCollection = firestore.collection("history")

    /**
     * Adds a food item to the user's history in Firebase.
     */
    suspend fun addFoodItem(foodItem: MenuItem): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                historyCollection.document(user.uid).collection("foodItems").add(foodItem).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves the user's food history from Firebase.
     */
    suspend fun getFoodHistory(): List<FoodItem> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val snapshot = historyCollection.document(user.uid).collection("foodItems").get().await()
                snapshot.documents.mapNotNull { it.toObject(FoodItem::class.java) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
