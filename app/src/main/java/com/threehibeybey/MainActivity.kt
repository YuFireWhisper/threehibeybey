package com.threehibeybey

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.threehibeybey.ui.theme.MyApplicationTheme
import java.io.IOException

data class FoodItem(val name: String, val price: Int, val calories: Float)
data class Category(val name: String, val items: List<FoodItem>)
data class Restaurant(val name: String, val image: String, val items: List<Category>)
data class SchoolCanteen(val name: String, val image: String, val items: List<Restaurant>)
data class BottomNavItem(val route: String, val title: String, val icon: Painter)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MyApp()
            }
        }
    }
}

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
            composable("restaurant") { SchoolCanteenScreen(navController) }
            composable(
                "restaurant/{canteenName}",
                arguments = listOf(navArgument("canteenName") { type = NavType.StringType })
            ) { backStackEntry ->
                val canteenName = backStackEntry.arguments?.getString("canteenName")
                RestaurantScreen(navController, canteenName)
            }
            composable(
                "restaurant/{canteenName}/{restaurantName}",
                arguments = listOf(navArgument("canteenName") { type = NavType.StringType }, navArgument("restaurantName") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName")
                CategoryScreen(navController, restaurantName)
            }
            composable(
                "food/{restaurantName}/{categoryName}",
                arguments = listOf(navArgument("restaurantName") { type = NavType.StringType }, navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName")
                val categoryName = backStackEntry.arguments?.getString("categoryName")
                FoodScreen(navController, restaurantName, categoryName)
            }
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

@Composable
fun PersonalScreen() {
    // 個人頁面的內容
}

@Composable
fun SchoolCanteenScreen(navController: NavController) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(schoolCanteens) { schoolCanteen ->
            SchoolCanteenCard(schoolCanteen) {
                navController.navigate("restaurant/${schoolCanteen.name}")
            }
        }
    }
}

@Composable
fun SchoolCanteenCard(schoolCanteen: SchoolCanteen, onClick:() -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Image(
                painter = getImagePainter(context = context, imageName = schoolCanteen.image),
                contentDescription = null,
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = schoolCanteen.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun getImagePainter(context: Context, imageName: String): Painter {
    val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    return if (resourceId != 0) {
        painterResource(id = resourceId)
    } else {
        painterResource(id = R.drawable.ic_restaurant)
    }
}

@Composable
fun RestaurantScreen(navController: NavController, canteenName: String?) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)
    val restaurants = schoolCanteens.find { it.name == canteenName }?.items ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantCard(restaurant) {
                navController.navigate("restaurant/${canteenName}/${restaurant.name}")
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Image(
                painter = getImagePainter(context = context, imageName = restaurant.image),
                contentDescription = null,
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun CategoryScreen(navController: NavController, restaurantName: String?) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)
    val restaurant = schoolCanteens.flatMap { it.items }.find { it.name == restaurantName }
    val categories = restaurant?.items ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category) {
                navController.navigate("food/${restaurantName}/${category.name}")
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FoodScreen(navController: NavController, restaurantName: String?, categoryName: String?) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)
    val restaurant = schoolCanteens.flatMap { it.items }.find { it.name == restaurantName }
    val category = restaurant?.items?.find { it.name == categoryName }
    val foodItems = category?.items ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(foodItems) { foodItem ->
            FoodCard(foodItem) {
                // 這裡可以處理食物項目點擊事件
                // 可以顯示食物項目的詳細資訊
            }
        }
    }
}

@Composable
fun FoodCard(foodItem: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Price: ${foodItem.price}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Calories: ${foodItem.calories}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun loadSchoolCanteens(context: Context): List<SchoolCanteen> {
    val json: String
    try {
        json = context.assets.open("restaurants.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        return emptyList()
    }

    val gson = Gson()
    val listType = object : TypeToken<List<SchoolCanteen>>() {}.type
    return gson.fromJson(json, listType)
}
