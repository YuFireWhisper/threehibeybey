package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.threehibeybey.composables.MyApp
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.repositories.HistoryRepository
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.utils.JsonLoader
import com.threehibeybey.viewmodels.AuthViewModel
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Main activity that hosts the application's navigation and screens.
 */
class MainActivity : ComponentActivity() {

    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var personalViewModel: PersonalViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Check if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || !currentUser.isEmailVerified) {
            // Not logged in or email not verified, navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize repositories
        val restaurantRepository = RestaurantRepository()
        val historyRepository = HistoryRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        val authRepository = AuthRepository(FirebaseAuth.getInstance())

        // Initialize view models
        personalViewModel = PersonalViewModel(historyRepository)
        restaurantViewModel = RestaurantViewModel(restaurantRepository)
        authViewModel = AuthViewModel(authRepository)

        // Load data before setting content
        restaurantViewModel.loadSchoolCanteens(JsonLoader(), this)

        setContent {
            MyApplicationTheme {
                MyApp(
                    restaurantViewModel = restaurantViewModel,
                    personalViewModel = personalViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        // Navigate to LoginActivity when user logs out
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
