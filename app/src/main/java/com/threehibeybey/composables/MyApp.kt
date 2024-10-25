package com.threehibeybey.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function that sets up the navigation graph and screens.
 */
@Composable
fun MyApp(
    restaurantViewModel: RestaurantViewModel,
    personalViewModel: PersonalViewModel
) {
    val navController = rememberNavController()
    var selectedFoods by remember { mutableStateOf(listOf<com.threehibeybey.models.FoodItem>()) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "restaurant",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("restaurant") {
                RestaurantScreen(
                    navController = navController,
                    restaurantViewModel = restaurantViewModel,
                    selectedFoods = selectedFoods,
                    setSelectedFoods = { selectedFoods = it }
                )
            }
            composable("calorieCalculator") {
                CalorieCalculatorScreen()
            }
            composable("personal") {
                PersonalScreen(
                    personalViewModel = personalViewModel,
                    onChangePassword = { /* Handle change password */ },
                    onChangeEmail = { /* Handle change email */ },
                    onDeleteAccount = { /* Handle delete account */ },
                    onViewHistory = { /* Navigate to history screen */ }
                )
            }
            composable(
                route = "food/{restaurantName}/{categoryName}",
                arguments = listOf(
                    navArgument("restaurantName") { type = NavType.StringType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName") ?: ""
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                FoodScreen(
                    navController = navController,
                    restaurantName = restaurantName,
                    categoryName = categoryName,
                    restaurantViewModel = restaurantViewModel,
                    selectedFoods = selectedFoods,
                    setSelectedFoods = { selectedFoods = it },
                    personalViewModel = personalViewModel
                )
            }
            // 新增的 composable 以處理 "restaurant/{restaurantName}" 路由
            composable(
                route = "restaurant/{restaurantName}",
                arguments = listOf(
                    navArgument("restaurantName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName") ?: ""
                CategoryScreen(
                    navController = navController,
                    restaurantName = restaurantName,
                    restaurantViewModel = restaurantViewModel,
                    selectedFoods = selectedFoods,
                    setSelectedFoods = { selectedFoods = it }
                )
            }
        }
    }
}
