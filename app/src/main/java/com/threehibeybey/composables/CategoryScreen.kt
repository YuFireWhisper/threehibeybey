package com.threehibeybey.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.Category
import com.threehibeybey.models.MenuItem
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Displays all categories for a specific restaurant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    restaurantName: String,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<MenuItem>,
    setSelectedFoods: (List<MenuItem>) -> Unit
) {
    val restaurants by restaurantViewModel.restaurants.collectAsState()

    // Find the corresponding restaurant
    val restaurant = restaurants.find { it.name == restaurantName }

    // Extract all categories from the restaurant
    val categories: List<Category> = restaurant?.items ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurantName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        content = { innerPadding ->
            if (categories.isEmpty()) {
                Text(
                    text = "找不到餐廳 $restaurantName 的分類",
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                Box(modifier = Modifier.padding(innerPadding)) {
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

                    if (selectedFoods.isNotEmpty()) {
                        FloatingSummaryCard(
                            selectedFoods = selectedFoods,
                            onClear = { setSelectedFoods(emptyList()) },
                            onConfirm = {
                                // Handle confirm action
                                // Save to history if needed
                            }
                        )
                    }
                }
            }
        }
    )
}

/**
 * Card component for each category.
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
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
