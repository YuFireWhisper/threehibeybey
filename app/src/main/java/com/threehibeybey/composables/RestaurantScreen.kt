package com.threehibeybey.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.threehibeybey.models.Restaurant
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for displaying the list of restaurants.
 */
@Composable
fun RestaurantScreen(
    navController: NavController,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<com.threehibeybey.models.MenuItem>, // 修改為 MenuItem
    setSelectedFoods: (List<com.threehibeybey.models.MenuItem>) -> Unit // 修改為 MenuItem
) {
    val canteens by restaurantViewModel.canteens.collectAsState()
    val error by restaurantViewModel.error.collectAsState()

    if (error != null) {
        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
    } else {
        if (canteens.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "載入中...")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 正確地迭代每個餐廳
                items(canteens.map { it.restaurant }) { restaurant ->
                    Log.d("RestaurantScreen", "載入餐廳: ${restaurant.name}")
                    RestaurantCard(restaurant) {
                        if (restaurant.name == "全家便利商店") {
                            // 導向新增品項的輸入畫面
                            navController.navigate("familyMartInput")
                        } else {
                            navController.navigate("restaurant/${restaurant.name}")
                        }
                    }
                }
            }

            // 懸浮卡片顯示邏輯
            if (selectedFoods.isNotEmpty()) {
                FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
            }
        }
    }
}

/**
 * Card composable for each restaurant.
 */
@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
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
                text = restaurant.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
