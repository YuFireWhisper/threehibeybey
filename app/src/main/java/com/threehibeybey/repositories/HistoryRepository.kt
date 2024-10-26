package com.threehibeybey.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.models.MenuItem
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling history-related data operations.
 *
 * @param firestore The Firebase Firestore instance.
 * @param firebaseAuth The Firebase Authentication instance.
 */
class HistoryRepository(private val firestore: FirebaseFirestore, private val firebaseAuth: FirebaseAuth) {

    /**
     * Saves the selected foods as a history item in Firebase.
     *
     * @param foods The list of selected menu items.
     * @param restaurantName The name of the restaurant.
     * @param timestamp The time when the items were saved.
     * @return True if the operation was successful, false otherwise.
     */
    suspend fun saveHistory(
        foods: List<MenuItem>,
        restaurantName: String,
        timestamp: Long
    ): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val historyItem = HistoryItem(
                    restaurantName = restaurantName,
                    items = foods,
                    totalCalories = foods.sumOf { it.calories.toDouble() },
                    totalPrice = foods.sumOf { it.price },
                    timestamp = timestamp
                )
                firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .add(historyItem)
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves the user's history from Firebase.
     *
     * @return A list of HistoryItem objects.
     */
    suspend fun getHistory(): List<HistoryItem> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val snapshot = firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .get()
                    .await()
                snapshot.toObjects(HistoryItem::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Adds a single food item to the user's history.
     *
     * @param foodItem The food item to add.
     * @return True if the operation was successful, false otherwise.
     */
    suspend fun addFoodItem(foodItem: MenuItem): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val historyItem = HistoryItem(
                    restaurantName = "單品項",
                    items = listOf(foodItem),
                    totalCalories = foodItem.calories.toDouble(),
                    totalPrice = foodItem.price,
                    timestamp = System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .add(historyItem)
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
