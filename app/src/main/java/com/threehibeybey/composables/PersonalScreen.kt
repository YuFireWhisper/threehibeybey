package com.threehibeybey.composables

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Ensure this is imported
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
import androidx.compose.ui.unit.dp // Ensure this is imported
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel

@Composable
fun PersonalScreen(
    authViewModel: AuthViewModel,
    onChangePassword: (String, String) -> Unit,
    onChangeEmail: (String, String) -> Unit,
    onDeleteAccount: (String) -> Unit,
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
            is AuthState.UpdateEmailSuccess -> {
                Toast.makeText(context, "電子郵件更新成功", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState() // Reset state
            }
            is AuthState.UpdatePasswordSuccess -> {
                Toast.makeText(context, "密碼更新成功", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            is AuthState.DeleteAccountSuccess -> {
                Toast.makeText(context, "帳號已刪除", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
                onLogout() // Perform logout action
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
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
            onConfirm = { currentPassword, newPassword ->
                onChangePassword(currentPassword, newPassword)
                showChangePasswordDialog = false
            },
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showChangeEmailDialog) {
        ChangeEmailDialog(
            onConfirm = { newEmail, currentPassword ->
                onChangeEmail(newEmail, currentPassword)
                showChangeEmailDialog = false
            },
            onDismiss = { showChangeEmailDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = { password ->
                onDeleteAccount(password)
                showDeleteAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
fun ChangePasswordDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("變更密碼") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        errorMessage = if (currentPassword.isNotEmpty()) {
                            ""
                        } else {
                            "請輸入當前密碼。"
                        }
                    },
                    label = { Text("當前密碼") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = if (newPassword.isNotEmpty()) {
                            ""
                        } else {
                            "請輸入新密碼。"
                        }
                    },
                    label = { Text("新密碼") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                    onConfirm(currentPassword, newPassword)
                }
            }) {
                Text("確認")
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
fun ChangeEmailDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("變更電子郵件") },
        text = {
            Column {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = {
                        newEmail = it
                        errorMessage = if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                            ""
                        } else {
                            "請輸入有效的電子郵件。"
                        }
                    },
                    label = { Text("新電子郵件") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        errorMessage = if (currentPassword.isNotEmpty()) {
                            ""
                        } else {
                            "請輸入當前密碼。"
                        }
                    },
                    label = { Text("當前密碼") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                    onConfirm(newEmail, currentPassword)
                }
            }) {
                Text("確認")
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
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = if (password.isNotEmpty()) {
                            ""
                        } else {
                            "請輸入密碼。"
                        }
                    },
                    label = { Text("密碼") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                Text("確認")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
