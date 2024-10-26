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
import com.threehibeybey.models.MenuItem
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Displays menu items for a specific category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    navController: NavController,
    restaurantName: String,
    categoryName: String,
    restaurantViewModel: RestaurantViewModel,
    selectedFoods: List<MenuItem>,
    setSelectedFoods: (List<MenuItem>) -> Unit,
    personalViewModel: PersonalViewModel
) {
    val restaurants by restaurantViewModel.restaurants.collectAsState()

    // Find the corresponding restaurant
    val restaurant = restaurants.find { it.name == restaurantName }

    // Find the corresponding category
    val category = restaurant?.items?.find { it.name == categoryName }
    val subcategories = category?.items ?: emptyList()

    // Extract menu items from subcategories
    val foods: List<MenuItem> = subcategories.flatMap { it.items }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        content = { innerPadding ->
            if (foods.isEmpty()) {
                Text(
                    text = "找不到餐廳 $restaurantName 中分類 $categoryName 的品項",
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
                        items(foods) { food ->
                            FoodCard(food = food, isSelected = selectedFoods.contains(food)) {
                                // Update selected food list
                                setSelectedFoods(
                                    if (selectedFoods.contains(food)) {
                                        selectedFoods - food // Deselect
                                    } else {
                                        selectedFoods + food // Select
                                    }
                                )
                            }
                        }
                    }

                    if (selectedFoods.isNotEmpty()) {
                        FloatingSummaryCard(
                            selectedFoods = selectedFoods,
                            onClear = { setSelectedFoods(emptyList()) },
                            onConfirm = {
                                // Save the selected foods to history
                                personalViewModel.saveSelectedFoods(
                                    selectedFoods = selectedFoods,
                                    restaurantName = restaurantName
                                )
                                setSelectedFoods(emptyList())
                            }
                        )
                    }
                }
            }
        }
    )
}

/**
 * Card component for each menu item.
 */
@Composable
fun FoodCard(food: MenuItem, isSelected: Boolean, onClick: () -> Unit) {
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
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
