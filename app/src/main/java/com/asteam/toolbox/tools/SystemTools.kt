package com.asteam.toolbox.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat

@Composable
fun SystemToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "battery" -> BatteryScreen()
            "storage" -> StorageScreen()
            "device" -> DeviceScreen()
            "sensors" -> SensorsListScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun BatteryScreen() {
    val context = LocalContext.current
    val battery = remember { context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
    val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
    val temperature = (battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
    val voltage = battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val statusText = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "در حال شارژ"
        BatteryManager.BATTERY_STATUS_FULL -> "کامل"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "در حال مصرف"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "متصل، بدون شارژ"
        else -> "نامشخص"
    }
    ToolHeader("باتری")
    ResultCard("شارژ", if (percent >= 0) "$percent٪" else "نامشخص", statusText)
    ResultCard("دما", "${DecimalFormat("0.0").format(temperature)} °C", "ولتاژ: $voltage mV")
}

@Composable
private fun StorageScreen() {
    val stat = remember { StatFs(Environment.getDataDirectory().absolutePath) }
    val total = stat.totalBytes; val available = stat.availableBytes; val used = total - available
    ToolHeader("فضای ذخیره‌سازی")
    ResultCard("کل", formatBytes(total)); ResultCard("مصرف‌شده", formatBytes(used)); ResultCard("آزاد", formatBytes(available))
}

@Composable
private fun DeviceScreen() {
    ToolHeader("مشخصات دستگاه")
    ResultCard("سازنده", Build.MANUFACTURER)
    ResultCard("مدل", Build.MODEL)
    ResultCard("Android", Build.VERSION.RELEASE)
    ResultCard("API", Build.VERSION.SDK_INT.toString())
    ResultCard("دستگاه", Build.DEVICE)
    ResultCard("ABI", Build.SUPPORTED_ABIS.joinToString())
}

@Composable
private fun SensorsListScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensors = remember { manager.getSensorList(android.hardware.Sensor.TYPE_ALL) }
    ToolHeader("سنسورهای دستگاه", "${sensors.size} سنسور شناسایی شد")
    sensors.forEach { sensor ->
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(sensor.name)
            Text("${sensor.vendor} | v${sensor.version}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
    }
}

private fun formatBytes(bytes: Long): String = "${DecimalFormat("0.00").format(bytes / 1024.0 / 1024.0 / 1024.0)} GB"
