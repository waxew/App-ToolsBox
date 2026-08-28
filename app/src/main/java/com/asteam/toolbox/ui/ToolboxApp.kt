package com.asteam.toolbox.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.data.ToolCatalog
import com.asteam.toolbox.data.ToolItem
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.screens.AboutScreen
import com.asteam.toolbox.ui.screens.ContactScreen
import com.asteam.toolbox.ui.screens.DrawerContent
import com.asteam.toolbox.ui.screens.DrawerDestination
import com.asteam.toolbox.ui.screens.HomeScreen
import com.asteam.toolbox.ui.screens.SettingsScreen
import com.asteam.toolbox.ui.screens.ToolRouter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val context = LocalContext.current
        val preferences = remember { UserPreferences(context.applicationContext) }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var activeDestination by remember { mutableStateOf(DrawerDestination.HOME) }
        var activeTool by remember { mutableStateOf<ToolItem?>(null) }
        var preferenceRevision by remember { mutableIntStateOf(0) }
        val favorites = remember(preferenceRevision) { preferences.favorites() }

        fun navigate(destination: DrawerDestination) {
            activeTool = null
            activeDestination = destination
            scope.launch { drawerState.close() }
        }

        BackHandler(enabled = drawerState.isOpen || activeTool != null || activeDestination != DrawerDestination.HOME) {
            when {
                drawerState.isOpen -> scope.launch { drawerState.close() }
                activeTool != null -> activeTool = null
                activeDestination != DrawerDestination.HOME -> activeDestination = DrawerDestination.HOME
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        preferences = preferences,
                        active = activeDestination,
                        onDestination = ::navigate,
                        onProfileChanged = { preferenceRevision++ },
                    )
                }
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                activeTool?.title ?: when (activeDestination) {
                                    DrawerDestination.HOME -> "جعبه ابزار"
                                    DrawerDestination.FAVORITES -> "علاقه‌مندی‌ها"
                                    DrawerDestination.SETTINGS -> "تنظیمات"
                                    DrawerDestination.ABOUT -> "درباره نرم‌افزار"
                                    DrawerDestination.CONTACT -> "ارتباط با ما"
                                },
                            )
                        },
                        navigationIcon = {
                            if (activeTool != null) {
                                IconButton(onClick = { activeTool = null }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "منو")
                                }
                            }
                        },
                    )
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    val tool = activeTool
                    if (tool != null) {
                        Box(Modifier.padding(horizontal = 16.dp)) { ToolRouter(tool, preferences) }
                    } else {
                        when (activeDestination) {
                            DrawerDestination.HOME -> HomeScreen(
                                tools = ToolCatalog.tools,
                                favorites = favorites,
                                onOpenTool = { activeTool = it },
                                onToggleFavorite = { preferences.toggleFavorite(it.id); preferenceRevision++ },
                            )
                            DrawerDestination.FAVORITES -> HomeScreen(
                                tools = ToolCatalog.tools.filter { it.id in favorites },
                                favorites = favorites,
                                onOpenTool = { activeTool = it },
                                onToggleFavorite = { preferences.toggleFavorite(it.id); preferenceRevision++ },
                            )
                            DrawerDestination.SETTINGS -> SettingsScreen()
                            DrawerDestination.ABOUT -> AboutScreen()
                            DrawerDestination.CONTACT -> ContactScreen()
                        }
                    }
                }
            }
        }
    }
}
