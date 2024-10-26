package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.threehibeybey.composables.MyApp
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.repositories.HistoryRepository
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.viewmodels.AuthViewModel
import com.threehibeybey.viewmodels.PersonalViewModel
import com.threehibeybey.viewmodels.PreferenceViewModel
import com.threehibeybey.viewmodels.RestaurantViewModel

class MainActivity : ComponentActivity() {

    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var personalViewModel: PersonalViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferenceViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

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
        preferenceViewModel = PreferenceViewModel()

        // Load restaurant data
        restaurantViewModel.loadSchoolCanteens()

        setContent {
            MyApplicationTheme {
                MyApp(
                    restaurantViewModel = restaurantViewModel,
                    personalViewModel = personalViewModel,
                    authViewModel = authViewModel,
                    preferenceViewModel = preferenceViewModel,
                    onLogout = {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}