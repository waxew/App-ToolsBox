package com.asteam.toolbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B87),
    onPrimary = Color.White,
    secondary = Color(0xFFB36B00),
    tertiary = Color(0xFF386A20),
    background = Color(0xFFF7F9FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EEF2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF72D2F2),
    secondary = Color(0xFFFFB95C),
    tertiary = Color(0xFFA4D18A),
    background = Color(0xFF0F1418),
    surface = Color(0xFF151C21),
    surfaceVariant = Color(0xFF253139),
)

@Composable
fun ToolsBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
