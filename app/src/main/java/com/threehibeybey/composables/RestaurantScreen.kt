package com.threehibeybey.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.models.Restaurant
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for displaying the list of restaurants.
 */
@Composable
fun RestaurantScreen(
    navController: NavController,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<com.threehibeybey.models.FoodItem>,
    setSelectedFoods: (List<com.threehibeybey.models.FoodItem>) -> Unit
) {
    val canteens by restaurantViewModel.canteens.collectAsState()
    val error by restaurantViewModel.error.collectAsState()

    if (error != null) {
        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(canteens) { canteen ->
                RestaurantCard(canteen) {
                    navController.navigate("restaurant/${canteen.name}")
                }
            }
        }

        // 懸浮卡片顯示邏輯
        if (selectedFoods.isNotEmpty()) {
            FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
        }
    }
}

/**
 * Card composable for each canteen.
 */
@Composable
fun RestaurantCard(canteen: SchoolCanteen, onClick: () -> Unit) {
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
                text = canteen.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Floating summary card displaying total price and calories.
 */
@Composable
fun FloatingSummaryCard(selectedFoods: List<com.threehibeybey.models.FoodItem>, onClear: () -> Unit) {
    val totalAmount = selectedFoods.sumOf { it.price }
    val totalCalories = selectedFoods.sumOf { it.calories.toDouble() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "金額: $totalAmount 元",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "熱量: $totalCalories 大卡",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Button(
                    onClick = onClear,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("清除")
                }
            }
        }
    }
}
