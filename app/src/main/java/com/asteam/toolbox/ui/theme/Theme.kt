package com.asteam.toolbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Small accent palette used to keep every selectable accent visually coherent. */
private data class AccentPalette(
    val primaryLight: Color,
    val primaryDark: Color,
    val containerLight: Color,
    val containerDark: Color,
)

private fun accent(name: String): AccentPalette = when (name) {
    "green" -> AccentPalette(Color(0xFF2E6D3B), Color(0xFF8DDA9B), Color(0xFFD9F2DC), Color(0xFF173F23))
    "orange" -> AccentPalette(Color(0xFF9A5700), Color(0xFFFFB86B), Color(0xFFFFE2BF), Color(0xFF563000))
    "purple" -> AccentPalette(Color(0xFF6750A4), Color(0xFFD0BCFF), Color(0xFFEADDFF), Color(0xFF3F2E6E))
    else -> AccentPalette(Color(0xFF176B87), Color(0xFF72D2F2), Color(0xFFCDECF6), Color(0xFF164352))
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
    val palette = accent(accentColor)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primaryDark,
            onPrimary = Color(0xFF09252D),
            primaryContainer = palette.containerDark,
            onPrimaryContainer = Color(0xFFE8F7FB),
            secondary = Color(0xFFFFB95C),
            secondaryContainer = Color(0xFF513812),
            tertiary = Color(0xFFA4D18A),
            tertiaryContainer = Color(0xFF26451A),
            background = Color(0xFF111315),
            surface = Color(0xFF171A1D),
            surfaceVariant = Color(0xFF262D32),
            outlineVariant = Color(0xFF3B474E),
        )
    } else {
        lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = Color.White,
            primaryContainer = palette.containerLight,
            onPrimaryContainer = Color(0xFF102A32),
            secondary = Color(0xFFB36B00),
            secondaryContainer = Color(0xFFFFE1B7),
            tertiary = Color(0xFF386A20),
            tertiaryContainer = Color(0xFFD8EFC9),
            background = Color(0xFFFAF8F4),
            surface = Color(0xFFFFFCF8),
            surfaceVariant = Color(0xFFF0ECE6),
            outlineVariant = Color(0xFFD5CEC4),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
