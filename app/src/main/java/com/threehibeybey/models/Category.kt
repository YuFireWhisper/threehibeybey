package com.threehibeybey.models

/**
 * 資料類別表示分類，包含多個子分類。
 */
data class Category(
    val name: String = "",
    val items: List<SubcategoryWrapper> = emptyList()
)