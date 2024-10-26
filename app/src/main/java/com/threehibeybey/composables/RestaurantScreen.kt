package com.threehibeybey.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.Restaurant
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for displaying the list of restaurants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreen(
    navController: NavController,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<com.threehibeybey.models.MenuItem>,
    setSelectedFoods: (List<com.threehibeybey.models.MenuItem>) -> Unit
) {
    val restaurants by restaurantViewModel.restaurants.collectAsState()
    val error by restaurantViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("餐廳") },
                // No navigation icon as this is the main screen
            )
        },
        content = { innerPadding ->
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                if (restaurants.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "載入中...")
                    }
                } else {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(restaurants) { restaurant ->
                                Log.d("RestaurantScreen", "載入餐廳: ${restaurant.name}")
                                RestaurantCard(restaurant) {
                                    if (restaurant.name == "全家便利商店") {
                                        navController.navigate("familyMartInput")
                                    } else {
                                        navController.navigate("restaurant/${restaurant.name}")
                                    }
                                }
                            }
                        }

                        if (selectedFoods.isNotEmpty()) {
                            FloatingSummaryCard(
                                selectedFoods = selectedFoods,
                                onClear = { setSelectedFoods(emptyList()) },
                                onConfirm = {
                                    // Handle confirm action if needed
                                    // For RestaurantScreen, you may not need to handle confirm here
                                }
                            )
                        }
                    }
                }
            }
        }
    )
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
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
