// composables/FoodScreen.kt
package com.threehibeybey.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.threehibeybey.models.MenuItem
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * 顯示特定分類內的菜單項目。
 */
@Composable
fun FoodScreen(
    restaurantName: String,
    categoryName: String,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<MenuItem>,
    setSelectedFoods: (List<MenuItem>) -> Unit,
    personalViewModel: PersonalViewModel
) {
    val canteens by restaurantViewModel.canteens.collectAsState()

    // 找到對應的餐廳
    val restaurant = canteens
        .flatMap { it.restaurant.let { listOf(it) } }
        .find { it.name == restaurantName }

    // 找到對應的分類
    val categoryWrapper = restaurant?.items?.find { it.category.name == categoryName }
    val subcategories = categoryWrapper?.category?.items ?: emptyList()

    // 從子分類中提取菜單項目
    val foods: List<MenuItem> = subcategories.flatMap { subcategoryWrapper ->
        subcategoryWrapper.subcategory.items.map { it.menu_item }
    }

    if (foods.isEmpty()) {
        Log.e("FoodScreen", "No foods found for category: $categoryName in restaurant: $restaurantName")
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(foods) { food ->
            FoodCard(food = food, isSelected = selectedFoods.contains(food)) {
                // 更新選中的食物清單
                setSelectedFoods(
                    if (selectedFoods.contains(food)) {
                        selectedFoods - food // 取消選中
                    } else {
                        selectedFoods + food // 選中
                    }
                )
                // 使用 personalViewModel 添加至歷史紀錄
                personalViewModel.addToHistory(food)
            }
        }
    }

    // 懸浮卡片顯示邏輯
    if (selectedFoods.isNotEmpty()) {
        FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
    }
}

/**
 * 每個菜單項目的卡片組件。
 */
@Composable
fun FoodCard(food: MenuItem, isSelected: Boolean, onClick: () -> Unit)  {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "價格: ${food.price} 元",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "熱量: ${food.calories} 大卡",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
