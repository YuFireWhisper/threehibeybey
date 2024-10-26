package com.threehibeybey.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.models.SchoolCanteen
import java.io.IOException

/**
 * @deprecated Data is now loaded from Firebase Firestore instead of local JSON files.
 * This class is kept for reference only.
 */
@Deprecated("Data is now loaded from Firebase Firestore. Use PreferencesRepository instead.")
class JsonLoader {
    @Deprecated("Use PreferencesRepository.getRestaurants() instead",
        ReplaceWith("PreferencesRepository(FirebaseFirestore.getInstance()).getRestaurants()",
            "com.threehibeybey.repositories.PreferencesRepository",
            "com.google.firebase.firestore.FirebaseFirestore"))
    fun loadJson(context: Context, fileName: String): List<SchoolCanteen> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val schoolCanteenType = object : TypeToken<List<SchoolCanteen>>() {}.type
                gson.fromJson(json, schoolCanteenType)
            }
        } catch (e: IOException) {
            Log.e("JsonLoader", "Error reading JSON file: ${e.localizedMessage}")
            emptyList()
        } catch (e: Exception) {
            Log.e("JsonLoader", "Error parsing JSON: ${e.localizedMessage}")
            emptyList()
        }
    }
}