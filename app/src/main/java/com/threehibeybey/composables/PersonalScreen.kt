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
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.PasswordResetEmailSent -> {
                Toast.makeText(context, "已發送密碼重置郵件，請檢查您的電子郵件。", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            is AuthState.EmailChangeEmailSent -> {
                Toast.makeText(context, "已發送電子郵件變更確認，請檢查您的電子郵件。", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            is AuthState.DeleteAccountEmailSent -> {
                Toast.makeText(context, "已發送帳號刪除確認，請檢查您的電子郵件。", Toast.LENGTH_SHORT).show()
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
            onClick = { showChangePasswordDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("變更密碼")
        }

        Button(
            onClick = { showChangeEmailDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("變更電子郵件")
        }

        Button(
            onClick = { showDeleteAccountDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("刪除帳號")
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

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            currentEmail = authViewModel.user.value?.email ?: "",
            onConfirm = { email ->
                authViewModel.sendPasswordResetEmail(email)
                showChangePasswordDialog = false
            },
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showChangeEmailDialog) {
        ChangeEmailDialog(
            onConfirm = {
                authViewModel.sendEmailChangeRequest()
                showChangeEmailDialog = false
            },
            onDismiss = { showChangeEmailDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = { password ->
                authViewModel.sendDeleteAccountEmail(password)
                showDeleteAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
fun ChangePasswordDialog(currentEmail: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var email by remember { mutableStateOf(currentEmail) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("變更密碼") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = if (email == currentEmail) {
                            ""
                        } else {
                            "電子郵件與目前使用者不符。"
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

@Composable
fun ChangeEmailDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("變更電子郵件") },
        text = {
            Text("我們將發送電子郵件變更確認到您的目前電子郵件地址。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("刪除帳號") },
        text = {
            Column {
                Text("請輸入您的密碼以驗證身份，驗證成功後將發送帳號刪除確認到您的電子郵件。")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = if (password.isNotEmpty()) {
                            ""
                        } else {
                            "密碼不能為空。"
                        }
                    },
                    label = { Text("密碼") },
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
                    onConfirm(password)
                }
            }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
