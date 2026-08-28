package com.asteam.toolbox.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val sensorFormat = DecimalFormat("0.00")

@Composable
fun MeasurementToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "compass" -> CompassScreen()
            "level" -> LevelScreen()
            "light" -> ScalarSensorScreen(title, Sensor.TYPE_LIGHT, "lux")
            "magnetic" -> VectorSensorScreen(title, Sensor.TYPE_MAGNETIC_FIELD, "µT")
            "pressure" -> ScalarSensorScreen(title, Sensor.TYPE_PRESSURE, "hPa")
            "altitude" -> AltitudeScreen()
            "accelerometer" -> VectorSensorScreen(title, Sensor.TYPE_ACCELEROMETER, "m/s²")
            "gyroscope" -> VectorSensorScreen(title, Sensor.TYPE_GYROSCOPE, "rad/s")
            "flashlight" -> FlashlightScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun ScalarSensorScreen(title: String, type: Int, unit: String) {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(type) }
    var value by remember { mutableFloatStateOf(0f) }
    ToolHeader(title, if (sensor == null) "این سنسور روی دستگاه موجود نیست." else "داده زنده سنسور")
    DisposableEffect(sensor) {
        if (sensor == null) onDispose { }
        else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) { value = event.values.firstOrNull() ?: 0f }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    if (sensor != null) ResultCard("مقدار", "${sensorFormat.format(value)} $unit", sensor.name)
}

@Composable
private fun VectorSensorScreen(title: String, type: Int, unit: String) {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(type) }
    var values by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    ToolHeader(title, if (sensor == null) "این سنسور روی دستگاه موجود نیست." else "داده زنده سه محور")
    DisposableEffect(sensor) {
        if (sensor == null) onDispose { }
        else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) { values = event.values.copyOf(3) }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    if (sensor != null) {
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        ResultCard("شدت", "${sensorFormat.format(magnitude)} $unit", "X: ${sensorFormat.format(values[0])} | Y: ${sensorFormat.format(values[1])} | Z: ${sensorFormat.format(values[2])}")
    }
}

@Composable
private fun CompassScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    var gravity by remember { mutableStateOf<FloatArray?>(null) }
    var magnetic by remember { mutableStateOf<FloatArray?>(null) }
    var azimuth by remember { mutableFloatStateOf(0f) }
    ToolHeader("قطب‌نما", if (accelerometer == null || magnetometer == null) "سنسورهای لازم موجود نیستند." else "برای کالیبراسیون، دستگاه را به شکل عدد ۸ حرکت دهید.")
    DisposableEffect(accelerometer, magnetometer) {
        if (accelerometer == null || magnetometer == null) onDispose { }
        else {
            fun update() {
                val g = gravity ?: return; val m = magnetic ?: return
                val rotation = FloatArray(9); val inclination = FloatArray(9)
                if (SensorManager.getRotationMatrix(rotation, inclination, g, m)) {
                    val orientation = FloatArray(3); SensorManager.getOrientation(rotation, orientation)
                    var degrees = Math.toDegrees(orientation[0].toDouble()).toFloat(); if (degrees < 0) degrees += 360f
                    azimuth = degrees
                }
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values.copyOf() else magnetic = event.values.copyOf()
                    update()
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            manager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    if (accelerometer != null && magnetometer != null) {
        val direction = when (azimuth) {
            in 337.5f..360f, in 0f..<22.5f -> "شمال"
            in 22.5f..<67.5f -> "شمال‌شرق"
            in 67.5f..<112.5f -> "شرق"
            in 112.5f..<157.5f -> "جنوب‌شرق"
            in 157.5f..<202.5f -> "جنوب"
            in 202.5f..<247.5f -> "جنوب‌غرب"
            in 247.5f..<292.5f -> "غرب"
            else -> "شمال‌غرب"
        }
        ResultCard("جهت", "${azimuth.roundToInt()}°", direction)
    }
}

@Composable
private fun LevelScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var values by remember { mutableStateOf(floatArrayOf(0f, 0f, 9.8f)) }
    ToolHeader("تراز", if (sensor == null) "شتاب‌سنج موجود نیست." else "گوشی را روی سطح قرار دهید.")
    DisposableEffect(sensor) {
        if (sensor == null) onDispose { }
        else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) { values = event.values.copyOf(3) }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    if (sensor != null) {
        val x = values[0]; val y = values[1]; val z = values[2]
        val pitch = Math.toDegrees(atan2(-x.toDouble(), sqrt((y * y + z * z).toDouble())))
        val roll = Math.toDegrees(atan2(y.toDouble(), z.toDouble()))
        ResultCard("شیب", "X ${sensorFormat.format(pitch)}° | Y ${sensorFormat.format(roll)}°", if (kotlin.math.abs(pitch) < 1 && kotlin.math.abs(roll) < 1) "تقریباً تراز" else "نیاز به تنظیم")
    }
}

@Composable
private fun AltitudeScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_PRESSURE) }
    var pressure by remember { mutableFloatStateOf(SensorManager.PRESSURE_STANDARD_ATMOSPHERE) }
    ToolHeader("ارتفاع‌سنج", if (sensor == null) "فشارسنج روی دستگاه موجود نیست." else "ارتفاع تقریبی بر پایه فشار استاندارد سطح دریا")
    DisposableEffect(sensor) {
        if (sensor == null) onDispose { }
        else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) { pressure = event.values[0] }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    if (sensor != null) ResultCard("ارتفاع تقریبی", "${sensorFormat.format(SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure))} m", "فشار: ${sensorFormat.format(pressure)} hPa")
}

@Composable
private fun FlashlightScreen() {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember {
        runCatching { cameraManager.cameraIdList.firstOrNull { cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true } }.getOrNull()
    }
    var enabled by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it }
    ToolHeader("چراغ‌قوه", if (cameraId == null) "فلش دوربین در دسترس نیست." else "کنترل فلش اصلی دستگاه")
    DisposableEffect(cameraId) { onDispose { if (cameraId != null && enabled) runCatching { cameraManager.setTorchMode(cameraId, false) } } }
    if (cameraId != null) {
        Button(
            onClick = {
                if (!permissionGranted) launcher.launch(Manifest.permission.CAMERA)
                else {
                    val next = !enabled
                    if (runCatching { cameraManager.setTorchMode(cameraId, next) }.isSuccess) enabled = next
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (enabled) "خاموش کردن" else "روشن کردن") }
        ResultCard("وضعیت", if (enabled) "روشن" else "خاموش")
    }
}
