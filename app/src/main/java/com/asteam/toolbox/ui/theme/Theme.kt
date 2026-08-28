package com.asteam.toolbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun accent(name: String, dark: Boolean): Color = when (name) {
    "green" -> if (dark) Color(0xFF8DDA9B) else Color(0xFF2E6D3B)
    "orange" -> if (dark) Color(0xFFFFB86B) else Color(0xFF9A5700)
    "purple" -> if (dark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
    else -> if (dark) Color(0xFF72D2F2) else Color(0xFF176B87)
}

/** Persisted theme mode + accent color without changing the rest of app data. */
@Composable
fun ToolsBoxTheme(
    themeMode: String = "system",
    accentColor: String = "blue",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val primary = accent(accentColor, darkTheme)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            secondary = Color(0xFFFFB95C),
            tertiary = Color(0xFFA4D18A),
            background = Color(0xFF0F1418),
            surface = Color(0xFF151C21),
            surfaceVariant = Color(0xFF253139),
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            secondary = Color(0xFFB36B00),
            tertiary = Color(0xFF386A20),
            background = Color(0xFFF7F9FB),
            surface = Color.White,
            surfaceVariant = Color(0xFFE7EEF2),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
