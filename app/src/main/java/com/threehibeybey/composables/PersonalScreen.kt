package com.threehibeybey.composables

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel

@Composable
fun PersonalScreen(
    authViewModel: AuthViewModel,
    onViewHistory: () -> Unit,
    onLogout: () -> Unit
) {
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.PasswordResetEmailSent -> {
                Toast.makeText(context, "已發送密碼重置郵件，請檢查您的電子郵件。", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "個人設置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = { showResetPasswordDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("重置密碼")
        }

        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("歷史紀錄")
        }

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("登出")
        }
    }

    if (showResetPasswordDialog) {
        ResetPasswordDialog(
            onConfirm = { email ->
                authViewModel.sendPasswordResetEmail(email)
                showResetPasswordDialog = false
            },
            onDismiss = { showResetPasswordDialog = false }
        )
    }
}

@Composable
fun ResetPasswordDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置密碼") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            ""
                        } else {
                            "請輸入有效的電子郵件。"
                        }
                    },
                    label = { Text("電子郵件") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (errorMessage.isEmpty()) {
                    onConfirm(email)
                }
            }) {
                Text("發送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
