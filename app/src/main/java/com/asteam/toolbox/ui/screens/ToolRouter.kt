package com.asteam.toolbox.ui.screens

import androidx.compose.runtime.Composable
import com.asteam.toolbox.data.ToolCategory
import com.asteam.toolbox.data.ToolItem
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.tools.CalculationToolScreen
import com.asteam.toolbox.tools.ConverterToolScreen
import com.asteam.toolbox.tools.EverydayToolScreen
import com.asteam.toolbox.tools.MeasurementToolScreen
import com.asteam.toolbox.tools.SystemToolScreen

@Composable
fun ToolRouter(tool: ToolItem, preferences: UserPreferences) {
    when (tool.category) {
        ToolCategory.MEASUREMENT -> MeasurementToolScreen(tool.id, tool.title)
        ToolCategory.CALCULATION -> CalculationToolScreen(tool.id, tool.title)
        ToolCategory.CONVERSION -> ConverterToolScreen(tool.id, tool.title)
        ToolCategory.TIME_DATE, ToolCategory.DIGITAL -> EverydayToolScreen(tool.id, tool.title, preferences)
        ToolCategory.SYSTEM -> SystemToolScreen(tool.id, tool.title)
    }
}
