package com.threehibeybey.models

/**
 * 資料類別表示子分類，包含多個菜單項目。
 */
data class Subcategory(
    val name: String = "",
    val items: List<MenuItem> = emptyList()
)
