package com.threehibeybey.models

/**
 * 資料類別表示餐廳，包含多個分類。
 */
data class Restaurant(
    val name: String = "",
    val items: List<Category> = emptyList()
)
