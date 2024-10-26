package com.threehibeybey.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.viewmodels.PersonalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    personalViewModel: PersonalViewModel
) {
    val historyItems by personalViewModel.history.collectAsState()
    val historyState by personalViewModel.historyState.collectAsState()

    var selectedItemForEdit by remember { mutableStateOf<HistoryItem?>(null) }

    // 當畫面首次顯示時載入歷史紀錄
    LaunchedEffect(Unit) {
        personalViewModel.getHistory()
    }

    Scaffold(
        topBar = {
            BackButton(onBackClick = { navController.popBackStack() }, title = "歷史紀錄")
        },
        content = { innerPadding ->
            when (historyState) {
                is PersonalViewModel.HistoryState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is PersonalViewModel.HistoryState.Success -> {
                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyItems, key = { it.id }) { item ->
                            val dismissState = rememberDismissState(confirmValueChange = { newValue ->
                                if (newValue == DismissValue.DismissedToStart) {
                                    personalViewModel.deleteHistoryItem(item.id)
                                    true
                                } else {
                                    false
                                }
                            })
                            SwipeToDismiss(
                                state = dismissState,
                                background = {
                                    // 背景內容（例如刪除圖示）
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                directions = setOf(DismissDirection.EndToStart),
                                dismissContent = {
                                    HistoryCard(
                                        historyItem = item,
                                        onClick = {
                                            // 顯示詳細資訊對話框
                                            personalViewModel.selectHistoryItem(item)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
                is PersonalViewModel.HistoryState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (historyState as PersonalViewModel.HistoryState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    // 重置狀態以防止重複的錯誤訊息
                    LaunchedEffect(Unit) {
                        personalViewModel.resetHistoryState()
                    }
                }
                else -> {}
            }
        }
    )

    // 如果有選擇的項目，顯示詳細資訊對話框
    val selectedHistoryItem by personalViewModel.selectedHistoryItem.collectAsState()
    if (selectedHistoryItem != null) {
        HistoryDetailDialog(
            historyItem = selectedHistoryItem!!,
            onDismiss = { personalViewModel.selectHistoryItem(null) },
            onEdit = {
                selectedItemForEdit = selectedHistoryItem
                personalViewModel.selectHistoryItem(null)
            }
        )
    }

    // 如果有要編輯的項目，顯示編輯對話框
    if (selectedItemForEdit != null) {
        EditHistoryDialog(
            historyItem = selectedItemForEdit!!,
            onConfirm = { updatedItem ->
                personalViewModel.updateHistoryItem(updatedItem.id, updatedItem)
                selectedItemForEdit = null
            },
            onDismiss = { selectedItemForEdit = null }
        )
    }
}

@Composable
fun HistoryCard(
    historyItem: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("餐廳：${historyItem.restaurantName}", style = MaterialTheme.typography.bodyLarge)
            Text("時間：${formatTimestamp(historyItem.timestamp)}", style = MaterialTheme.typography.bodyMedium)
            Text("總金額：${historyItem.totalPrice} 元", style = MaterialTheme.typography.bodyMedium)
            Text("總熱量：${historyItem.totalCalories} 大卡", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("品項：", style = MaterialTheme.typography.bodyMedium)
            historyItem.items.forEach { item ->
                Text("- ${item.name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HistoryDetailDialog(
    historyItem: HistoryItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("詳細資訊") },
        text = {
            Column {
                Text("餐廳：${historyItem.restaurantName}")
                Text("時間：${formatTimestamp(historyItem.timestamp)}")
                Text("總金額：${historyItem.totalPrice} 元")
                Text("總熱量：${historyItem.totalCalories} 大卡")
                Spacer(modifier = Modifier.height(8.dp))
                Text("品項：")
                historyItem.items.forEach { item ->
                    Text("- ${item.name}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("關閉")
            }
        },
        dismissButton = {
            TextButton(onClick = onEdit) {
                Text("編輯")
            }
        }
    )
}

@Composable
fun EditHistoryDialog(
    historyItem: HistoryItem,
    onConfirm: (HistoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var restaurantName by remember { mutableStateOf(historyItem.restaurantName) }
    var items by remember { mutableStateOf(historyItem.items.toMutableList()) }
    var totalPrice by remember { mutableStateOf(historyItem.totalPrice.toString()) }
    var totalCalories by remember { mutableStateOf(historyItem.totalCalories.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編輯歷史紀錄") },
        text = {
            Column {
                // 餐廳名稱
                TextFieldWithLabel(
                    label = "餐廳名稱",
                    value = restaurantName,
                    onValueChange = { restaurantName = it }
                )
                // 總金額
                TextFieldWithLabel(
                    label = "總金額",
                    value = totalPrice,
                    onValueChange = { totalPrice = it }
                )
                // 總熱量
                TextFieldWithLabel(
                    label = "總熱量",
                    value = totalCalories,
                    onValueChange = { totalCalories = it }
                )
                // 品項（簡化示例）
                Spacer(modifier = Modifier.height(8.dp))
                Text("品項：")
                items.forEachIndexed { index, menuItem ->
                    TextFieldWithLabel(
                        label = "品項 ${index + 1}",
                        value = menuItem.name,
                        onValueChange = { newName ->
                            items[index] = menuItem.copy(name = newName)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedItem = historyItem.copy(
                    restaurantName = restaurantName,
                    totalPrice = totalPrice.toIntOrNull() ?: historyItem.totalPrice,
                    totalCalories = totalCalories.toDoubleOrNull() ?: historyItem.totalCalories,
                    items = items
                )
                onConfirm(updatedItem)
            }) {
                Text("保存")
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
fun TextFieldWithLabel(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(text = label)
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
