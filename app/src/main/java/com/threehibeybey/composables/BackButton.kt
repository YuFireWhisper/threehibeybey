package com.threehibeybey.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

/**
 * A reusable TopAppBar with a back button.
 *
 * @param onBackClick Lambda function to handle back button click.
 * @param title The title to display in the TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackButton(onBackClick: () -> Unit, title: String) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回上一頁")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}
