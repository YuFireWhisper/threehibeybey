package com.threehibeybey.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryOrange,
    background = BackgroundGrey,
    onBackground = TextBlack,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryOrange,
    background = Color(0xFF303030),
    onBackground = Color(0xFFE0E0E0),
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
