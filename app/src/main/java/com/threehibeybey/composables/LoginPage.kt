package com.threehibeybey.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Composable function for the login page.
 */
@Composable
fun LoginPage(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: (String) -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            ""
                        } else {
                            "無效的電子郵件格式"
                        }
                    },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = emailError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                if (emailError.isNotEmpty()) {
                    Text(
                        text = emailError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = if (password.length >= 8) {
                            ""
                        } else {
                            "密碼長度至少為8位"
                        }
                    },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (emailError.isEmpty() && passwordError.isEmpty()) {
                                onLoginClick(email, password)
                            }
                        }
                    )
                )
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                TextButton(
                    onClick = {
                        if (email.isNotEmpty()) {
                            onForgotPasswordClick(email)
                        } else {
                            emailError = "請先輸入您的電子郵件"
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "忘記密碼？",
                        textDecoration = TextDecoration.Underline
                    )
                }

                Button(
                    onClick = {
                        if (emailError.isEmpty() && passwordError.isEmpty()) {
                            onLoginClick(email, password)
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("登入")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRegisterClick) {
                    Text("註冊")
                }
            }
        }
    }
}
