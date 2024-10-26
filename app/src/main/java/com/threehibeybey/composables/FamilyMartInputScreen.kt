package com.threehibeybey.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.threehibeybey.models.MenuItem
import com.threehibeybey.viewmodels.RestaurantViewModel

/**
 * Composable function for inputting new Menu Items for "全家便利商店".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMartInputScreen(
    navController: NavController,
    restaurantViewModel: RestaurantViewModel
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var caloriesError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增全家便利商店品項") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("品項名稱") },
                    placeholder = { Text("輸入品項名稱") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = null
                    },
                    label = { Text("價格 (元)") },
                    placeholder = { Text("輸入價格") },
                    singleLine = true,
                    isError = priceError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (priceError != null) {
                    Text(
                        text = priceError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                OutlinedTextField(
                    value = calories,
                    onValueChange = {
                        calories = it
                        caloriesError = null
                    },
                    label = { Text("熱量 (大卡)") },
                    placeholder = { Text("輸入熱量") },
                    singleLine = true,
                    isError = caloriesError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (caloriesError != null) {
                    Text(
                        text = caloriesError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Button(
                    onClick = {
                        var isValid = true

                        if (name.isBlank()) {
                            nameError = "名稱不能為空"
                            isValid = false
                        }
                        val priceValue = price.toIntOrNull()
                        if (priceValue == null || priceValue <= 0) {
                            priceError = "請輸入有效的價格"
                            isValid = false
                        }
                        val caloriesValue = calories.toDoubleOrNull()
                        if (caloriesValue == null || caloriesValue <= 0) {
                            caloriesError = "請輸入有效的熱量"
                            isValid = false
                        }

                        if (isValid) {
                            val newFood = MenuItem(
                                name = name,
                                price = priceValue!!,
                                calories = caloriesValue!!
                            )
                            restaurantViewModel.addFamilyMartFoodItem(newFood)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("新增品項")
                }
            }
        }
    )
}

