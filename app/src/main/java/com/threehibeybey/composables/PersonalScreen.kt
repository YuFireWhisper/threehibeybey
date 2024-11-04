package com.threehibeybey.composables

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel
import com.threehibeybey.viewmodels.PreferenceViewModel

data class SettingsOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val type: OptionType = OptionType.NORMAL
)

enum class OptionType {
    NORMAL,
    DANGEROUS,
    ACCENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    authViewModel: AuthViewModel,
    onViewHistory: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit
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
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            is AuthState.AccountDeleted -> {
                onLogout()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("個人設置") },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "資料與紀錄",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val dataOptions = listOf(
                SettingsOption(
                    "歷史紀錄",
                    Icons.Default.History,
                    onViewHistory,
                    OptionType.ACCENT
                )
            )

            SettingsGroup(dataOptions)

            Text(
                "帳號管理",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val accountOptions = listOf(
                SettingsOption(
                    "變更密碼",
                    Icons.Default.Lock,
                    { showChangePasswordDialog = true }
                ),
                SettingsOption(
                    "變更電子郵件",
                    Icons.Default.Email,
                    { showChangeEmailDialog = true }
                )
            )

            SettingsGroup(accountOptions)

            Spacer(modifier = Modifier.weight(1f))

            val dangerOptions = listOf(
                SettingsOption(
                    "登出",
                    Icons.AutoMirrored.Filled.ExitToApp,
                    onLogout,
                    OptionType.DANGEROUS
                ),
                SettingsOption(
                    "刪除帳號",
                    Icons.Default.Delete,
                    { showDeleteAccountDialog = true },
                    OptionType.DANGEROUS
                )
            )

            SettingsGroup(dangerOptions)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = { password ->
                authViewModel.deleteAccount(password)
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
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
}

@Composable
fun SettingsGroup(options: List<SettingsOption>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            options.forEachIndexed { index, option ->
                SettingsItem(
                    option = option,
                    showDivider = index < options.size - 1
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    option: SettingsOption,
    showDivider: Boolean
) {
    Column {
        ListItem(
            headlineContent = {
                Text(
                    option.title,
                    color = when (option.type) {
                        OptionType.DANGEROUS -> MaterialTheme.colorScheme.error
                        OptionType.ACCENT -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            },
            leadingContent = {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = when (option.type) {
                        OptionType.DANGEROUS -> MaterialTheme.colorScheme.error
                        OptionType.ACCENT -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            modifier = Modifier.clickable(onClick = option.onClick)
        )
        if (showDivider) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun PreferencesGroup(
    preferences: Map<String, Any>,
    preferenceViewModel: PreferenceViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            preferences.forEach { (key, value) ->
                PreferenceItem(
                    key = key,
                    value = value,
                    onValueChange = { newValue ->
                        preferenceViewModel.updatePreference(key, newValue)
                    }
                )
            }
        }
    }
}

@Composable
fun PreferenceItem(
    key: String,
    value: Any,
    onValueChange: (Any) -> Unit
) {
    // For simplicity, assume all preferences are Boolean
    val isChecked = value as? Boolean ?: false
    ListItem(
        headlineContent = { Text(key) },
        trailingContent = {
            androidx.compose.material3.Switch(
                checked = isChecked,
                onCheckedChange = { onValueChange(it) }
            )
        }
    )
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
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
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
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("刪除帳號") },
        text = {
            Column {
                Text(
                    "請輸入您的密碼以確認刪除帳號。\n注意：此操作將直接刪除您的帳號且無法復原！",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "隱藏密碼" else "顯示密碼"
                            )
                        }
                    }
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isNotEmpty()) {
                        onConfirm(password)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("確認刪除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
