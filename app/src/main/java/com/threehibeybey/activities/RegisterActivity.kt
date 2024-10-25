package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.threehibeybey.composables.RegisterPage
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
                    isLoading = authViewModel.authState.collectAsState().value is AuthState.Loading,
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
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        // Show loading indicator if necessary
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@RegisterActivity, "註冊成功，請登入。", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}
