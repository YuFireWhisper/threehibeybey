package com.threehibeybey.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Composable function for the calorie calculator screen UI.
 */
@Composable
fun CalorieCalculatorScreen() {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "熱量計算機",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = height,
            onValueChange = {
                height = it
                heightError = null
            },
            label = { Text("身高 (公分)") },
            placeholder = { Text("輸入您的身高") },
            singleLine = true,
            isError = heightError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (heightError != null) {
            Text(
                text = heightError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                weightError = null
            },
            label = { Text("體重 (公斤)") },
            placeholder = { Text("輸入您的體重") },
            singleLine = true,
            isError = weightError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (weightError != null) {
            Text(
                text = weightError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Button(
            onClick = {
                val heightValue = height.toFloatOrNull()
                val weightValue = weight.toFloatOrNull()
                var isValid = true

                if (heightValue == null || heightValue <= 0) {
                    heightError = "請輸入有效的身高"
                    isValid = false
                }
                if (weightValue == null || weightValue <= 0) {
                    weightError = "請輸入有效的體重"
                    isValid = false
                }

                if (isValid) {
                    val heightInMeters = heightValue?.div(100)
                    val bmi = weightValue?.div((heightInMeters?.times(heightInMeters)!!))
                    bmiResult = String.format(Locale.getDefault(), "%.2f", bmi)
                    focusManager.clearFocus()
                } else {
                    bmiResult = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = heightError == null && weightError == null
        ) {
            Text("計算 BMI")
        }

        bmiResult?.let {
            Text(
                text = "您的 BMI 是：$it",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
