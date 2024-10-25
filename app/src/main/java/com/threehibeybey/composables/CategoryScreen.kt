package com.threehibeybey.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.Category
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for displaying the list of categories within a restaurant.
 */
@Composable
fun CategoryScreen(
    navController: NavController,
    restaurantName: String,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<com.threehibeybey.models.FoodItem>,
    setSelectedFoods: (List<com.threehibeybey.models.FoodItem>) -> Unit
) {
    val canteens by restaurantViewModel.canteens.collectAsState()
    val categories = canteens
        .flatMap { it.items }
        .filterIsInstance<com.threehibeybey.models.Restaurant>()
        .find { it.name == restaurantName }
        ?.items ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category) {
                navController.navigate("food/${restaurantName}/${category.name}")
            }
        }
    }

    // 懸浮卡片顯示邏輯
    if (selectedFoods.isNotEmpty()) {
        FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
    }
}

/**
 * Card composable for each category.
 */
@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
