package com.threehibeybey.composables

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
import androidx.navigation.NavController
import com.threehibeybey.models.Category
import com.threehibeybey.models.FoodItem
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for displaying the list of food items within a category.
 */
@Composable
fun FoodScreen(
    navController: NavController,
    restaurantName: String,
    categoryName: String,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<FoodItem>,
    setSelectedFoods: (List<FoodItem>) -> Unit,
    personalViewModel: com.threehibeybey.viewmodels.PersonalViewModel
) {
    val canteens by restaurantViewModel.canteens.collectAsState()
    val foods: List<FoodItem> = canteens
        .flatMap { it.items }
        .filterIsInstance<com.threehibeybey.models.Restaurant>()
        .find { it.name == restaurantName }
        ?.items
        ?.flatMap { it.items }
        ?.filterIsInstance<Category>()
        ?.find { it.name == categoryName }
        ?.items ?: emptyList()

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
 * Card composable for each food item.
 */
@Composable
fun FoodCard(food: FoodItem, isSelected: Boolean, onClick: () -> Unit) {
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
