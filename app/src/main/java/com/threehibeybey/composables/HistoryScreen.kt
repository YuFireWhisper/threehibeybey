package com.threehibeybey.composables

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
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
    val context = LocalContext.current

    // Load history when the screen is displayed
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
                        CircularProgressIndicator(
                            modifier = Modifier.semantics { contentDescription = "載入中" }
                        )
                    }
                }
                is PersonalViewModel.HistoryState.Success -> {
                    if (historyItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "目前沒有歷史紀錄",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = innerPadding,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(historyItems, key = { it.id }) { item ->
                                val dismissState = rememberDismissState(confirmValueChange = { newValue ->
                                    if (newValue == DismissValue.DismissedToEnd) {
                                        personalViewModel.deleteHistoryItem(item.id)
                                        true
                                    } else {
                                        false
                                    }
                                })
                                SwipeToDismiss(
                                    state = dismissState,
                                    background = {
                                        // Background content (e.g., delete icon)
                                        val alignment = Alignment.CenterStart
                                        val iconModifier = Modifier.padding(start = 16.dp)
                                        DeleteBackground(alignment, iconModifier)
                                    },
                                    directions = setOf(DismissDirection.StartToEnd),
                                    dismissContent = {
                                        HistoryCard(
                                            historyItem = item,
                                            onClick = {
                                                // Show detail dialog
                                                personalViewModel.selectHistoryItem(item)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                is PersonalViewModel.HistoryState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (historyState as PersonalViewModel.HistoryState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    // Reset state to prevent duplicate error messages
                    LaunchedEffect(Unit) {
                        personalViewModel.resetHistoryState()
                    }
                }
                else -> {}
            }
        }
    )

    // Show detail dialog if an item is selected
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

    // Show edit dialog if an item is selected for editing
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("餐廳：${historyItem.restaurantName}", style = MaterialTheme.typography.titleMedium)
            Text("時間：${formatTimestamp(historyItem.timestamp)}", style = MaterialTheme.typography.bodySmall)
            Text("總金額：${historyItem.totalPrice} 元", style = MaterialTheme.typography.bodySmall)
            Text("總熱量：${historyItem.totalCalories} 大卡", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("品項：", style = MaterialTheme.typography.bodySmall)
            historyItem.items.forEach { item ->
                Text("- ${item.name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DeleteBackground(alignment: Alignment, modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "刪除",
            tint = MaterialTheme.colorScheme.onError,
            modifier = modifier
        )
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
    var totalPrice by remember { mutableStateOf(historyItem.totalPrice.toString()) }
    var totalCalories by remember { mutableStateOf(historyItem.totalCalories.toString()) }
    var items by remember { mutableStateOf(historyItem.items.toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編輯歷史紀錄") },
        text = {
            Column {
                OutlinedTextField(
                    value = restaurantName,
                    onValueChange = { restaurantName = it },
                    label = { Text("餐廳名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalPrice,
                    onValueChange = { totalPrice = it },
                    label = { Text("總金額") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalCalories,
                    onValueChange = { totalCalories = it },
                    label = { Text("總熱量") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("品項：", style = MaterialTheme.typography.bodyMedium)
                items.forEachIndexed { index, menuItem ->
                    OutlinedTextField(
                        value = menuItem.name,
                        onValueChange = { newName ->
                            items[index] = menuItem.copy(name = newName)
                        },
                        label = { Text("品項 ${index + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}