package com.threehibeybey.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.models.MenuItem
import kotlinx.coroutines.tasks.await

class HistoryRepository(private val firestore: FirebaseFirestore, private val firebaseAuth: FirebaseAuth) {

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
                    totalCalories = foods.sumOf { it.calories },
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

    suspend fun getHistory(): List<HistoryItem> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val snapshot = firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .get()
                    .await()
                snapshot.documents.map { document ->
                    val item = document.toObject(HistoryItem::class.java)!!
                    item.id = document.id // Set the ID of the history item
                    item
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteHistoryItem(itemId: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .document(itemId)
                    .delete()
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateHistoryItem(itemId: String, updatedItem: HistoryItem): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .document(itemId)
                    .set(updatedItem)
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
                    totalCalories = foodItem.calories,
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
