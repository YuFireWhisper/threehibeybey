package com.threehibeybey.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.utils.JsonLoader
import java.io.IOException

/**
 * Repository for handling restaurant-related data operations.
 */
class RestaurantRepository {

    /**
     * Loads the school canteens from the JSON file using JsonLoader.
     */
    suspend fun loadCanteens(jsonLoader: JsonLoader, context: Context): List<SchoolCanteen> {
        return jsonLoader.loadJson(context, "restaurants.json")
    }
}
