package com.threehibeybey.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
 * Composable function for displaying the list of restaurants in a school canteen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanteenScreen(
    navController: NavController,
    canteenName: String,
    restaurantViewModel: RestaurantViewModel
) {
    val schoolCanteens by restaurantViewModel.schoolCanteens.collectAsState()

    val canteen = schoolCanteens.find { it.name == canteenName }
    val restaurants: List<Restaurant> = canteen?.items ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(canteenName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            if (restaurants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "找不到學餐 $canteenName 的餐廳",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                            RestaurantCard(restaurant) {
                                if (restaurant.name == "全家便利商店") {
                                    navController.navigate("familyMartInput")
                                } else {
                                    navController.navigate("restaurant/${canteenName}/${restaurant.name}")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
