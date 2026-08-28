package com.asteam.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.ToolboxApp
import com.asteam.toolbox.ui.theme.ToolsBoxTheme

/** Single-activity entry point for the Compose application. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val preferences = remember { UserPreferences(applicationContext) }
            var settingsRevision by remember { mutableIntStateOf(0) }
            val themeMode = remember(settingsRevision) { preferences.themeMode }
            val accentColor = remember(settingsRevision) { preferences.accentColor }

            ToolsBoxTheme(themeMode = themeMode, accentColor = accentColor) {
                ToolboxApp(
                    preferences = preferences,
                    onPreferencesChanged = { settingsRevision++ },
                )
            }
        }
    }
}
