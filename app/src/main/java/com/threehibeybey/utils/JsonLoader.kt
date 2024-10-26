package com.threehibeybey.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.models.SchoolCanteen
import java.io.IOException

/**
 * Utility class for loading and parsing JSON data from assets.
 */
@Deprecated("Preferences are now loaded from Firebase. This class is kept for legacy purposes.")
class JsonLoader {

    /**
     * Loads and parses a JSON file into a list of SchoolCanteen objects.
     *
     * @param context The application context.
     * @param fileName The name of the JSON file in the assets directory.
     * @return A list of SchoolCanteen objects, or an empty list if an error occurs.
     */
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
