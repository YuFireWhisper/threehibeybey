// utils/JsonLoader.kt
package com.threehibeybey.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.models.SchoolCanteen
import java.io.IOException

/**
 * 工具類別用於從assets加載和解析JSON數據。
 */
class JsonLoader {

    /**
     * 加載並解析JSON文件為SchoolCanteen物件列表。
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

            val schoolCanteenType = object : TypeToken<SchoolCanteen>() {}.type
            val schoolCanteen: SchoolCanteen = gson.fromJson(json, schoolCanteenType)
            listOf(schoolCanteen)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
