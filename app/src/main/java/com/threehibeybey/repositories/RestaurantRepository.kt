package com.threehibeybey.repositories

import android.content.Context
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.utils.JsonLoader

/**
 * Repository responsible for handling restaurant-related data operations.
 */
class RestaurantRepository {

    /**
     * Loads school canteens from a JSON file.
     */
    suspend fun loadSchoolCanteens(jsonLoader: JsonLoader, context: Context): List<SchoolCanteen> {
        return jsonLoader.loadJson(context, "restaurants.json")
    }
}
