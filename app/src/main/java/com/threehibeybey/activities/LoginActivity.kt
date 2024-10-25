package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.threehibeybey.composables.LoginPage
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel

/**
 * Activity responsible for handling user login.
 */
class LoginActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        authViewModel = AuthViewModel(authRepository)

        setContent {
            MyApplicationTheme {
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    authViewModel.authState.collect { state ->
                        when (state) {
                            is AuthState.Loading -> {
                                isLoading = true
                            }
                            is AuthState.Success -> {
                                isLoading = false
                                // 登入成功，導向 MainActivity
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                            is AuthState.Error -> {
                                isLoading = false
                                Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            }
                            is AuthState.Idle -> {
                                isLoading = false
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LoginPage(
                        onLoginClick = { email, password ->
                            authViewModel.login(email, password)
                        },
                        onRegisterClick = {
                            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                        },
                        isLoading = isLoading
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
