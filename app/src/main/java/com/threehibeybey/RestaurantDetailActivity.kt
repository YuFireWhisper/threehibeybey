package com.threehibeybey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.ui.theme.MyApplicationTheme

class RestaurantDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val restaurantName = intent.getStringExtra("restaurant_name") ?: ""
                val itemsJson = intent.getStringExtra("restaurant_items") ?: "[]"
                val type = object : TypeToken<List<MenuItem>>() {}.type
                val items: List<MenuItem> = Gson().fromJson(itemsJson, type)
                RestaurantDetail(restaurantName, items)
            }

        }
    }
}

@Composable
fun RestaurantDetail(restaurantName: String, items: List<MenuItem>) {
    Column {
        Text(
            text = restaurantName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = TextAlign.Center
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                MenuItemCard(item)
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = item.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "金額: ${item.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "熱量: ${item.calories} 大卡", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
