package com.threehibeybey.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.threehibeybey.viewmodels.PersonalViewModel

/**
 * Composable function for the personal screen UI.
 */
@Composable
fun PersonalScreen(
    personalViewModel: PersonalViewModel = viewModel(),
    onChangePassword: () -> Unit,
    onChangeEmail: () -> Unit,
    onDeleteAccount: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "個人設置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("變更密碼")
        }

        Button(
            onClick = onChangeEmail,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("變更電子郵件")
        }

        Button(
            onClick = onDeleteAccount,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("刪除帳號")
        }

        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("歷史紀錄")
        }
    }
}
