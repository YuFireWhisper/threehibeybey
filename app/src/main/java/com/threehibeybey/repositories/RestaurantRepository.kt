package com.threehibeybey.repositories

import android.content.Context
import com.threehibeybey.models.Restaurant
import com.threehibeybey.utils.JsonLoader

/**
 * Repository 負責處理餐廳相關的數據操作。
 */
class RestaurantRepository {

    /**
     * 使用 JsonLoader 從 JSON 文件加載餐廳列表。
     */
    suspend fun loadRestaurants(jsonLoader: JsonLoader, context: Context): List<Restaurant> {
        return jsonLoader.loadJson(context, "restaurants.json")
    }
}
