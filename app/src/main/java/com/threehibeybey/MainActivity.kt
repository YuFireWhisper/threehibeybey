package com.threehibeybey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threehibeybey.ui.theme.MyApplicationTheme
import kotlinx.serialization.Serializable
import java.io.IOException
import java.nio.charset.Charset

@Serializable
data class Restaurant(
    val name: String,
    val description: String,
    val image: String,
    val items: List<MenuItem>
)

@Serializable
data class MenuItem(
    val name: String,
    val price: Int,
    val calories: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MyApp()
            }
        }
    }

    private fun loadRestaurants(context: Context): List<Restaurant> {
        val json: String
        try {
            val inputStream = context.assets.open("restaurants.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return emptyList()
        }
        val type = object : TypeToken<List<Restaurant>>() {}.type
        return Gson().fromJson(json, type)
    }
}

@Preview
@Composable
fun MyApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "personal",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("personal") { PersonalScreen() }
            composable("restaurant") { RestaurantScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("personal", "個人", painterResource(id = R.drawable.ic_personal)),
        BottomNavItem("restaurant", "餐廳", painterResource(id = R.drawable.ic_restaurant))
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.title, style = MaterialTheme.typography.labelSmall)
                    }
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false
            )
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val icon: Painter)

@Composable
fun PersonalScreen() {
    // 個人頁面的內容
}

@Composable
fun RestaurantScreen() {
    val context = LocalContext.current
    val restaurants = remember { loadRestaurants(context) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantCard(restaurant) { selectedRestaurant ->
                val intent = Intent(context, RestaurantDetailActivity::class.java).apply {
                    putExtra("restaurant_name", selectedRestaurant.name)
                    putExtra("restaurant_items", Gson().toJson(selectedRestaurant.items))
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick(restaurant) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Image(
                painter = getImagePainter(restaurant.image),
                contentDescription = null,
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun getImagePainter(imageName: String): Painter {
    val imageResource = when (imageName) {
        "ic_restaurant.png" -> R.drawable.ic_restaurant
        // 可以增加其他圖片的對應
        else -> R.drawable.ic_restaurant // 預設圖片
    }
    return painterResource(id = imageResource)
}

fun loadRestaurants(context: Context): List<Restaurant> {
    val json: String
    try {
        val inputStream = context.assets.open("restaurants.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charset.forName("UTF-8"))
    } catch (ex: IOException) {
        ex.printStackTrace()
        return emptyList()
    }
    val type = object : TypeToken<List<Restaurant>>() {}.type
    return Gson().fromJson(json, type)
}
