package com.example.retrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color.Black,
    tertiary = Color(0xFFCCCCCC),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF2C2C2C),
    outlineVariant = Color(0xFF1E1E1E)
)

private val MinimalLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = Color(0xFF333333),
    onSecondary = Color.White,
    tertiary = Color(0xFF555555),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF7F7F7),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0)
)

@Composable
fun RetrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AmoledDarkColorScheme else MinimalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
