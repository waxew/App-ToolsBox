package com.asteam.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.asteam.toolbox.ui.ToolboxApp
import com.asteam.toolbox.ui.theme.ToolsBoxTheme

/** Single-activity entry point for the Compose application. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ToolsBoxTheme { ToolboxApp() }
        }
    }
}
