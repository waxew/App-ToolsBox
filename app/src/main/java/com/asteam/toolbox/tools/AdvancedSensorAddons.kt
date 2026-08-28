package com.asteam.toolbox.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import kotlin.math.max

/** Extra measurement tools completing the v1.2 roadmap inside v2. */
@Composable
fun AdvancedSensorAddonScreen(toolId: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (toolId) {
            "light_graph" -> LightGraphScreen()
            "gps_heading" -> GpsHeadingScreen()
            "calibrated_altimeter" -> CalibratedAltimeterScreen()
        }
    }
}

@Composable
private fun LightGraphScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    var lux by remember { mutableFloatStateOf(0f) }
    var samples by remember { mutableStateOf(List(60) { 0f }) }

    ToolHeader("نمودار زنده نور", if (sensor == null) "سنسور نور روی دستگاه موجود نیست." else "۶۰ نمونه آخر شدت نور محیط")

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    lux = event.values.firstOrNull() ?: 0f
                    samples = (samples.drop(1) + lux)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    if (sensor != null) {
        ResultCard("نور", "${"%.1f".format(lux)} lux")
        val lineColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val peak = max(samples.maxOrNull() ?: 1f, 1f)
            val path = Path()
            samples.forEachIndexed { index, value ->
                val x = if (samples.size <= 1) 0f else size.width * index / (samples.size - 1).toFloat()
                val y = size.height - (value / peak).coerceIn(0f, 1f) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
        }
    }
}

@Composable
private fun GpsHeadingScreen() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    if (!granted) {
        ToolHeader("جهت GPS", "برای Heading و سرعت حرکت به Location نیاز است.")
        Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.Text("اجازه دسترسی به موقعیت")
        }
        return
    }

    val manager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var location by remember { mutableStateOf<Location?>(null) }

    DisposableEffect(manager) {
        val listener = LocationListener { location = it }
        runCatching {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000L, 0f, listener, Looper.getMainLooper())
        }
        onDispose { runCatching { manager.removeUpdates(listener) } }
    }

    val bearing = location?.bearing ?: 0f
    val direction = when (bearing) {
        in 337.5f..360f, in 0f..<22.5f -> "شمال"
        in 22.5f..<67.5f -> "شمال‌شرق"
        in 67.5f..<112.5f -> "شرق"
        in 112.5f..<157.5f -> "جنوب‌شرق"
        in 157.5f..<202.5f -> "جنوب"
        in 202.5f..<247.5f -> "جنوب‌غرب"
        in 247.5f..<292.5f -> "غرب"
        else -> "شمال‌غرب"
    }

    ToolHeader("جهت GPS", "Bearing فقط هنگام حرکت معتبرتر است.")
    if (location == null) ResultCard("GPS", "در انتظار موقعیت…")
    else {
        ResultCard("جهت حرکت", "${bearing.toInt()}°", direction)
        ResultCard("سرعت", "${"%.1f".format((location?.speed ?: 0f) * 3.6f)} km/h")
    }
}

@Composable
private fun CalibratedAltimeterScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_PRESSURE) }
    var pressure by remember { mutableFloatStateOf(SensorManager.PRESSURE_STANDARD_ATMOSPHERE) }
    var seaLevel by remember { mutableStateOf(SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString()) }

    ToolHeader("ارتفاع‌سنج کالیبره", if (sensor == null) "فشارسنج روی دستگاه موجود نیست." else "فشار سطح دریا را برای شرایط جوی محلی تنظیم کنید.")
    NumberField("فشار سطح دریا (hPa)", seaLevel, { seaLevel = it })

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) { pressure = event.values.firstOrNull() ?: pressure }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    if (sensor != null) {
        val reference = seaLevel.toFloatOrNull()?.takeIf { it > 0f } ?: SensorManager.PRESSURE_STANDARD_ATMOSPHERE
        val altitude = SensorManager.getAltitude(reference, pressure)
        ResultCard("ارتفاع تقریبی", "${"%.1f".format(altitude)} m", "فشار فعلی: ${"%.1f".format(pressure)} hPa")
    }
}
