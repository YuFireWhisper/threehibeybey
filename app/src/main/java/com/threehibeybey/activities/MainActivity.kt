package com.threehibeybey.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.composables.MyApp
import com.threehibeybey.repositories.HistoryRepository
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.utils.JsonLoader
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Main activity that hosts the application's navigation and screens.
 */
class MainActivity : ComponentActivity() {

    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var personalViewModel: PersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restaurantRepository = RestaurantRepository()
        val historyRepository = HistoryRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        personalViewModel = PersonalViewModel(historyRepository)
        restaurantViewModel = RestaurantViewModel(restaurantRepository)

        setContent {
            MyApplicationTheme {
                MyApp(
                    restaurantViewModel = restaurantViewModel,
                    personalViewModel = personalViewModel
                )
            }
        }

        // Load and augment canteens
        restaurantViewModel.loadCanteens(JsonLoader(), this)
        restaurantViewModel.augmentCanteens()
    }
}
