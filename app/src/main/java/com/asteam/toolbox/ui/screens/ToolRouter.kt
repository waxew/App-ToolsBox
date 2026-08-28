package com.asteam.toolbox.ui.screens

import androidx.compose.runtime.Composable
import com.asteam.toolbox.data.ToolCategory
import com.asteam.toolbox.data.ToolItem
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.tools.AdvancedCalculationToolScreen
import com.asteam.toolbox.tools.AdvancedQrScreen
import com.asteam.toolbox.tools.BackupRestoreScreen
import com.asteam.toolbox.tools.CalculationToolScreen
import com.asteam.toolbox.tools.CameraToolScreen
import com.asteam.toolbox.tools.ConverterToolScreen
import com.asteam.toolbox.tools.EnhancedScannerToolScreen
import com.asteam.toolbox.tools.EverydayToolScreen
import com.asteam.toolbox.tools.ExtraUtilityToolScreen
import com.asteam.toolbox.tools.MeasurementToolScreen
import com.asteam.toolbox.tools.NetworkToolScreen
import com.asteam.toolbox.tools.PersianDateToolScreen
import com.asteam.toolbox.tools.ProfessionalMeasurementToolScreen
import com.asteam.toolbox.tools.SystemToolScreen
import com.asteam.toolbox.tools.TextDeveloperToolScreen

private val advancedCalculationIds = setOf(
    "compound_interest", "percent_change", "reverse_tax", "multi_discount", "payroll", "overtime",
    "exact_age_plus", "business_days", "shape_area", "solid_volume", "pythagorean", "quadratic",
)

private val professionalMeasurementIds = setOf(
    "ruler", "protractor", "angle_meter", "vibrometer", "gps_dashboard", "distance_tracker", "sound_meter",
)

private val textDeveloperIds = setOf(
    "digit_converter", "sort_lines", "dedupe_lines", "case_converter", "json_formatter",
    "url_codec", "html_codec", "uuid", "hash_suite", "text_compare",
)

private val networkIds = setOf(
    "network_state", "local_ip", "public_ip", "dns_lookup", "ping_host", "port_test", "wifi_info",
)

private val persianDateIds = setOf("jalali_today", "date_converter_fa", "jalali_diff", "weekday_finder")
private val extraUtilityIds = setOf("unix_time", "roman_number", "gcd_lcm", "slugify", "reverse_text", "line_numbering")

@Composable
fun ToolRouter(tool: ToolItem, preferences: UserPreferences) {
    when {
        tool.id == "qr" -> AdvancedQrScreen()
        tool.id == "qr_scanner" -> EnhancedScannerToolScreen(onlyQr = true, preferences = preferences)
        tool.id == "barcode_scanner" -> EnhancedScannerToolScreen(onlyQr = false, preferences = preferences)
        tool.id in setOf("scan_history", "magnifier", "mirror") -> CameraToolScreen(tool.id, tool.title, preferences)
        tool.id in professionalMeasurementIds -> ProfessionalMeasurementToolScreen(tool.id, tool.title)
        tool.id in advancedCalculationIds -> AdvancedCalculationToolScreen(tool.id, tool.title)
        tool.id in textDeveloperIds -> TextDeveloperToolScreen(tool.id, tool.title)
        tool.id in networkIds -> NetworkToolScreen(tool.id, tool.title)
        tool.id in persianDateIds -> PersianDateToolScreen(tool.id, tool.title)
        tool.id in extraUtilityIds -> ExtraUtilityToolScreen(tool.id, tool.title)
        tool.id == "backup_restore" -> BackupRestoreScreen(preferences)
        else -> when (tool.category) {
            ToolCategory.MEASUREMENT -> MeasurementToolScreen(tool.id, tool.title)
            ToolCategory.CALCULATION -> CalculationToolScreen(tool.id, tool.title)
            ToolCategory.CONVERSION -> ConverterToolScreen(tool.id, tool.title)
            ToolCategory.TIME_DATE, ToolCategory.DIGITAL -> EverydayToolScreen(tool.id, tool.title, preferences)
            ToolCategory.SYSTEM -> SystemToolScreen(tool.id, tool.title)
        }
    }
}
