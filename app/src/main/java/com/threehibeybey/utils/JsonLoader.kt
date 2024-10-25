package com.threehibeybey.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.models.SchoolCanteen
import java.io.IOException

/**
 * Utility class for loading JSON data from assets.
 */
class JsonLoader {

    /**
     * Loads and parses the JSON file into a list of SchoolCanteen objects.
     */
    fun loadJson(context: Context, fileName: String): List<SchoolCanteen> {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val gson = Gson()
            val listType = object : TypeToken<List<SchoolCanteen>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}
