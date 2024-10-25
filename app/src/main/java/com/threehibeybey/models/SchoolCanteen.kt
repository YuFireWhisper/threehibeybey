package com.threehibeybey.models

/**
 * 資料類別表示學餐，包含多個餐廳。
 */
data class SchoolCanteen(
    val restaurants: List<Restaurant> = emptyList()
)
