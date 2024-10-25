package com.threehibeybey.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.lifecycle.lifecycleScope
import com.threehibeybey.composables.LoginPage
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.ui.theme.MyApplicationTheme
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

/**
 * Activity responsible for handling user login.
 */
class LoginActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        authViewModel = AuthViewModel(authRepository)

        setContent {
            MyApplicationTheme {
                // 使用 Compose 的狀態來管理載入指示器
                var isLoading by remember { mutableStateOf(false) }

                // 監聽 authState 並更新 isLoading 狀態
                LaunchedEffect(Unit) {
                    authViewModel.authState.collect { state ->
                        when (state) {
                            is AuthState.Loading -> {
                                isLoading = true
                            }
                            is AuthState.Success -> {
                                isLoading = false
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
                        // 載入指示器覆蓋在 LoginPage 之上
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
