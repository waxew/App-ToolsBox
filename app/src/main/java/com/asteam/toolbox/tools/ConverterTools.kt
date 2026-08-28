package com.asteam.toolbox.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import kotlin.math.PI

private data class UnitDef(val symbol: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double)
private val converterFormat = DecimalFormat("#,##0.##########")

@Composable
fun ConverterToolScreen(toolId: String, title: String) {
    val units = remember(toolId) { unitDefinitions(toolId) }
    if (units.isEmpty()) { ToolHeader(title); return }
    var input by remember(toolId) { mutableStateOf("1") }
    var fromIndex by remember(toolId) { mutableStateOf(0) }
    var toIndex by remember(toolId) { mutableStateOf(if (units.size > 1) 1 else 0) }
    val value = input.replace(",", "").toDoubleOrNull() ?: 0.0
    val result = units[toIndex].fromBase(units[fromIndex].toBase(value))

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader(title, "تبدیل به‌صورت آفلاین و لحظه‌ای انجام می‌شود")
        NumberField("مقدار", input, { input = it })
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UnitSelector("از", units, fromIndex, { fromIndex = it }, Modifier.weight(1f))
            UnitSelector("به", units, toIndex, { toIndex = it }, Modifier.weight(1f))
        }
        ResultCard("نتیجه", "${converterFormat.format(result)} ${units[toIndex].symbol}", "${converterFormat.format(value)} ${units[fromIndex].symbol}")
    }
}

@Composable
private fun UnitSelector(label: String, units: List<UnitDef>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${units[selected].symbol}", modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { index, unit ->
                DropdownMenuItem(text = { Text(unit.symbol) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}

private fun linear(symbol: String, factorToBase: Double) = UnitDef(symbol, { it * factorToBase }, { it / factorToBase })

private fun unitDefinitions(id: String): List<UnitDef> = when (id) {
    "length" -> listOf(linear("mm", .001), linear("cm", .01), linear("m", 1.0), linear("km", 1000.0), linear("inch", .0254), linear("ft", .3048), linear("mile", 1609.344))
    "mass" -> listOf(linear("mg", .000001), linear("g", .001), linear("kg", 1.0), linear("oz", .028349523125), linear("lb", .45359237), linear("ton", 1000.0))
    "temperature" -> listOf(UnitDef("°C", { it }, { it }), UnitDef("°F", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }), UnitDef("K", { it - 273.15 }, { it + 273.15 }))
    "area" -> listOf(linear("cm²", .0001), linear("m²", 1.0), linear("km²", 1_000_000.0), linear("hectare", 10_000.0), linear("ft²", .09290304), linear("acre", 4046.8564224))
    "volume" -> listOf(linear("ml", .001), linear("L", 1.0), linear("m³", 1000.0), linear("cup (US)", .2365882365), linear("gallon (US)", 3.785411784))
    "speed" -> listOf(linear("m/s", 1.0), linear("km/h", 1.0 / 3.6), linear("mph", .44704), linear("knot", .5144444444))
    "time" -> listOf(linear("second", 1.0), linear("minute", 60.0), linear("hour", 3600.0), linear("day", 86400.0), linear("week", 604800.0))
    "data" -> listOf(linear("B", 1.0), linear("KB", 1024.0), linear("MB", 1024.0 * 1024.0), linear("GB", 1024.0 * 1024.0 * 1024.0), linear("TB", 1024.0 * 1024.0 * 1024.0 * 1024.0))
    "pressure_convert" -> listOf(linear("Pa", 1.0), linear("kPa", 1000.0), linear("bar", 100000.0), linear("psi", 6894.757293168), linear("atm", 101325.0), linear("mmHg", 133.322387415))
    "energy" -> listOf(linear("J", 1.0), linear("kJ", 1000.0), linear("cal", 4.184), linear("kcal", 4184.0), linear("Wh", 3600.0), linear("kWh", 3_600_000.0))
    "angle" -> listOf(UnitDef("degree", { it * PI / 180.0 }, { it * 180.0 / PI }), UnitDef("radian", { it }, { it }))
    else -> emptyList()
}
