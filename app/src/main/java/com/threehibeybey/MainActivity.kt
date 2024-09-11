package com.threehibeybey

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
data class Restaurant(val name: String, val items: List<Category>)
data class SchoolCanteen(val name: String, val items: List<Restaurant>)
data class BottomNavItem(val route: String, val title: String)

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
    val (selectedFoods, setSelectedFoods) = remember { mutableStateOf(listOf<FoodItem>()) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "personal",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("personal") { PersonalScreen() }
            composable("restaurant") { SchoolCanteenScreen(navController, selectedFoods, setSelectedFoods) }
            composable(
                "restaurant/{canteenName}",
                arguments = listOf(navArgument("canteenName") { type = NavType.StringType })
            ) { backStackEntry ->
                val canteenName = backStackEntry.arguments?.getString("canteenName")
                RestaurantScreen(navController, canteenName, selectedFoods, setSelectedFoods)
            }
            composable(
                "restaurant/{canteenName}/{restaurantName}",
                arguments = listOf(navArgument("canteenName") { type = NavType.StringType }, navArgument("restaurantName") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName")
                CategoryScreen(navController, restaurantName, selectedFoods, setSelectedFoods)
            }
            composable(
                "food/{restaurantName}/{categoryName}",
                arguments = listOf(navArgument("restaurantName") { type = NavType.StringType }, navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName")
                val categoryName = backStackEntry.arguments?.getString("categoryName")
                FoodScreen(navController, restaurantName, categoryName, selectedFoods, setSelectedFoods)
            }
        }
    }

    // 懸浮卡片顯示邏輯
    if (selectedFoods.isNotEmpty()) {
        FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
    }
}

@Composable
fun FloatingSummaryCard(selectedFoods: List<FoodItem>, onClear: () -> Unit) {
    val totalAmount = selectedFoods.sumOf { it.price }
    val totalCalories = selectedFoods.sumOf { it.calories.toDouble() }

    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the size of the screen
            .padding(16.dp) // Optional padding
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Align the card to the bottom center
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Space between items
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "金額: $totalAmount 元",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "熱量: $totalCalories 大卡",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Button(
                    onClick = onClear,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("清除")
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("personal", "個人"),
        BottomNavItem("restaurant", "餐廳")
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                label = { Text(text = item.title, style = MaterialTheme.typography.labelSmall) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) }, // 這裡加上基本圖標
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun PersonalScreen() {
    val (height, setHeight) = remember { mutableStateOf("") }
    val (weight, setWeight) = remember { mutableStateOf("") }
    val (bmi, setBmi) = remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "BMI 計算器", style = MaterialTheme.typography.headlineMedium)

        // 身高輸入
        Text("身高 (cm):")
        OutlinedTextField(
            value = height,
            onValueChange = { setHeight(it) },
            label = { Text("輸入身高") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 體重輸入
        Text("體重 (kg):")
        OutlinedTextField(
            value = weight,
            onValueChange = { setWeight(it) },
            label = { Text("輸入體重") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 計算 BMI 按鈕
        Button(onClick = {
            val heightValue = height.toFloatOrNull()
            val weightValue = weight.toFloatOrNull()
            if (heightValue != null && weightValue != null && heightValue > 0) {
                val heightInMeters = heightValue / 100
                val bmiValue = weightValue / (heightInMeters * heightInMeters)
                setBmi(bmiValue)
            } else {
                setBmi(null)
            }
        }) {
            Text("計算 BMI")
        }

        // 顯示 BMI 結果
        bmi?.let {
            Text(
                text = "您的 BMI 是: ${String.format("%.2f", it)}",
                style = MaterialTheme.typography.bodyMedium
            )
        } ?: run {
            Text(
                text = "請輸入身高和體重並點擊計算。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SchoolCanteenScreen(
    navController: NavController,
    selectedFoods: List<FoodItem>,
    setSelectedFoods: (List<FoodItem>) -> Unit
) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)

    var showFamilyMartDialog by remember { mutableStateOf(false) }
    var familyMartAmount by remember { mutableStateOf("") }
    var familyMartCalories by remember { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(schoolCanteens) { schoolCanteen ->
            if (schoolCanteen.name == "全家便利商店") {
                // 顯示對話框而非卡片
                if (showFamilyMartDialog) {
                    FamilyMartDialog(
                        amount = familyMartAmount,
                        calories = familyMartCalories,
                        onAmountChange = { familyMartAmount = it },
                        onCaloriesChange = { familyMartCalories = it },
                        onCancel = { showFamilyMartDialog = false },
                        onComplete = {
                            val amount = familyMartAmount.toIntOrNull()
                            val calories = familyMartCalories.toFloatOrNull()
                            if (amount != null && calories != null) {
                                // 增加至選中食物清單
                                setSelectedFoods(selectedFoods + FoodItem("全家便利商店", amount, calories))
                            }
                            showFamilyMartDialog = false
                        }
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { showFamilyMartDialog = true },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "全家便利商店",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            } else {
                SchoolCanteenCard(schoolCanteen) {
                    navController.navigate("restaurant/${schoolCanteen.name}")
                }
            }
        }
    }

    // 如果有選中的食物或有全家便利商店的數據，則顯示 FloatingSummaryCard
    if (selectedFoods.isNotEmpty()) {
        FloatingSummaryCard(selectedFoods = selectedFoods, onClear = { setSelectedFoods(emptyList()) })
    }
}

@Composable
fun FamilyMartDialog(
    amount: String,
    calories: String,
    onAmountChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("全家便利商店") },
        text = {
            Column {
                // 金額輸入框
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("花費金額 (元)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 熱量輸入框
                OutlinedTextField(
                    value = calories,
                    onValueChange = onCaloriesChange,
                    label = { Text("攝取熱量 (大卡)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onComplete,
                enabled = amount.isNotEmpty() && calories.isNotEmpty() && amount.toIntOrNull() != null && calories.toFloatOrNull() != null
            ) {
                Text("完成")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}

@Composable
fun SchoolCanteenCard(schoolCanteen: SchoolCanteen, onClick: () -> Unit) {
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
                text = schoolCanteen.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun RestaurantScreen(
    navController: NavController,
    canteenName: String?,
    selectedFoods: List<FoodItem>,
    setSelectedFoods: (List<FoodItem>) -> Unit
) {
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
                text = restaurant.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun CategoryScreen(
    navController: NavController,
    restaurantName: String?,
    selectedFoods: List<FoodItem>,
    setSelectedFoods: (List<FoodItem>) -> Unit
) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)
    val categories = schoolCanteens.flatMap { it.items }
        .find { it.name == restaurantName }?.items ?: emptyList()

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
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FoodScreen(
    navController: NavController,
    restaurantName: String?,
    categoryName: String?,
    selectedFoods: List<FoodItem>,
    setSelectedFoods: (List<FoodItem>) -> Unit
) {
    val context = LocalContext.current
    val schoolCanteens = loadSchoolCanteens(context)
    val foods = schoolCanteens.flatMap { it.items }
        .flatMap { it.items }
        .find { it.name == categoryName }?.items ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(foods) { food ->
            FoodCard(food = food, isSelected = selectedFoods.contains(food)) {
                // 更新選中的食物清單
                setSelectedFoods(
                    if (selectedFoods.contains(food)) {
                        selectedFoods - food // 取消選中
                    } else {
                        selectedFoods + food // 選中
                    }
                )
            }
        }
    }
}

@Composable
fun FoodCard(food: FoodItem, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Price: ${food.price}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Calories: ${food.calories}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun loadSchoolCanteens(context: Context): List<SchoolCanteen> {
    return try {
        val inputStream = context.assets.open("restaurants.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer, Charsets.UTF_8)
        val gson = Gson()
        val listType = object : TypeToken<List<SchoolCanteen>>() {}.type
        gson.fromJson(json, listType)
    } catch (e: IOException) {
        e.printStackTrace()
        emptyList()
    }
}
