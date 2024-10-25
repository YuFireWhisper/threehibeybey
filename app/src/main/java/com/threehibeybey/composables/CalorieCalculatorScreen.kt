package com.threehibeybey.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Composable function for the calorie calculator screen UI.
 */
@Composable
fun CalorieCalculatorScreen() {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "熱量計算機",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("身高 (cm)") },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("體重 (kg)") },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val heightValue = height.toFloatOrNull()
                val weightValue = weight.toFloatOrNull()
                if (heightValue != null && weightValue != null && heightValue > 0) {
                    val heightInMeters = heightValue / 100
                    bmi = weightValue / (heightInMeters * heightInMeters)
                } else {
                    bmi = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("計算 BMI")
        }

        if (bmi != null) {
            Text(
                text = "您的 BMI 是: ${String.format("%.2f", bmi)}",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "請輸入有效的身高和體重。",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
