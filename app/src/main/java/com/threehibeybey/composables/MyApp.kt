package com.threehibeybey.composables

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
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
    val (selectedFoods, setSelectedFoods) = remember { mutableStateOf(listOf<com.threehibeybey.models.FoodItem>()) }

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
                    setSelectedFoods = setSelectedFoods
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
            // Add more composable destinations as needed
        }
    }
}
