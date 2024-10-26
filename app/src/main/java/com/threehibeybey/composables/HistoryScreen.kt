package com.threehibeybey.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.HistoryItem
import com.threehibeybey.viewmodels.PersonalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function to display the user's history records.
 *
 * @param navController The NavController for navigation.
 * @param personalViewModel The ViewModel handling personal data.
 */
@Composable
fun HistoryScreen(
    navController: NavController,
    personalViewModel: PersonalViewModel
) {
    val historyItems by personalViewModel.history.collectAsState()
    val historyState by personalViewModel.historyState.collectAsState()

    // Load history when the screen is first displayed
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
                        items(historyItems) { item ->
                            HistoryCard(historyItem = item)
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
                    // Reset the historyState to prevent error message from reappearing
                    LaunchedEffect(Unit) {
                        personalViewModel.resetHistoryState()
                    }
                }
                else -> {}
            }
        }
    )
}

/**
 * Composable function to display a single history record as a card.
 *
 * @param historyItem The history record to display.
 */
@Composable
fun HistoryCard(historyItem: HistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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

/**
 * Helper function to format the timestamp into a readable date and time.
 *
 * @param timestamp The timestamp to format.
 * @return A formatted date and time string.
 */
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
