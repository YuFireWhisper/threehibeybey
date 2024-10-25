package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.threehibeybey.composables.RegisterPage
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity responsible for handling user registration.
 */
class RegisterActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        authViewModel = AuthViewModel(authRepository)

        setContent {
            MyApplicationTheme {
                RegisterPage(
                    onRegisterClick = { email, password, confirmPassword ->
                        if (password != confirmPassword) {
                            Toast.makeText(this, "密碼不匹配。", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.register(email, password)
                        }
                    }
                )
            }
        }

        observeViewModel()
    }

    /**
     * Observes changes in the AuthViewModel and updates UI accordingly.
     */
    private fun observeViewModel() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Show loading indicator if necessary
                }
                is AuthState.Success -> {
                    Toast.makeText(this, "註冊成功，請登入。", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
