package com.threehibeybey.utils

import android.content.Context
import com.google.gson.Gson
import com.threehibeybey.models.Restaurant
import java.io.IOException

/**
 * 工具類別用於從 assets 加載和解析 JSON 數據。
 */
class JsonLoader {

    /**
     * 加載並解析 JSON 文件為 Restaurant 物件列表。
     */
    fun loadJson(context: Context, fileName: String): List<Restaurant> {
        return try {
            val inputStream = context.assets.open(fileName)
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val restaurants = gson.fromJson(json, Array<Restaurant>::class.java).toList()
            restaurants
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
