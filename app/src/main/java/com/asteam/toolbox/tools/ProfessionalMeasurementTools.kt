package com.asteam.toolbox.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.asteam.toolbox.ui.components.ActionRow
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val professionalFormat = DecimalFormat("0.00")

/** Routes the professional measurement tools introduced in v1.2.0. */
@Composable
fun ProfessionalMeasurementToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "ruler" -> RulerScreen()
            "protractor" -> ProtractorScreen()
            "angle_meter" -> AngleMeterScreen()
            "vibrometer" -> VibrometerScreen()
            "gps_dashboard" -> LocationPermissionGate { GpsDashboardScreen() }
            "distance_tracker" -> LocationPermissionGate { DistanceTrackerScreen() }
            "sound_meter" -> MicrophonePermissionGate { SoundMeterScreen() }
            else -> ToolHeader(title)
        }
    }
}

/**
 * Physical ruler based on the display's reported X DPI.
 * OEM DPI reporting is not laboratory-calibrated, so this tool is intended for
 * practical approximate measurements and clearly communicates that limitation.
 */
@Composable
private fun RulerScreen() {
    val context = LocalContext.current
    val metrics = context.resources.displayMetrics
    val xDpi = metrics.xdpi.takeIf { it > 0f } ?: 160f
    val mmPx = xDpi / 25.4f
    val lineColor = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface.toArgbCompat()

    ToolHeader("خط‌کش", "مقیاس بر پایه DPI گزارش‌شده نمایشگر است؛ برای کار دقیق صنعتی کالیبراسیون فیزیکی لازم است.")

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        val maxMillimeters = (size.width / mmPx).toInt().coerceAtLeast(1)
        val baseline = size.height - 12.dp.toPx()
        drawLine(lineColor, Offset(0f, baseline), Offset(size.width, baseline), 2.dp.toPx())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 12.dp.toPx()
            textAlign = Paint.Align.CENTER
        }

        for (mm in 0..maxMillimeters) {
            val x = mm * mmPx
            if (x > size.width) break
            val tickHeight = when {
                mm % 10 == 0 -> 34.dp.toPx()
                mm % 5 == 0 -> 24.dp.toPx()
                else -> 14.dp.toPx()
            }
            drawLine(
                color = if (mm % 10 == 0) accent else lineColor,
                start = Offset(x, baseline),
                end = Offset(x, baseline - tickHeight),
                strokeWidth = if (mm % 10 == 0) 2.dp.toPx() else 1.dp.toPx(),
            )
            if (mm % 10 == 0) {
                drawContext.canvas.nativeCanvas.drawText((mm / 10).toString(), x, baseline - 42.dp.toPx(), paint)
            }
        }
    }

    val visibleCm = (metrics.widthPixels / xDpi * 2.54f)
    ResultCard("عرض تقریبی قابل اندازه‌گیری", "${professionalFormat.format(visibleCm)} cm", "هر خط کوچک = ۱ میلی‌متر")
}

/** Interactive 0..180 degree protractor for geometry and visual comparison. */
@Composable
private fun ProtractorScreen() {
    var angle by remember { mutableFloatStateOf(45f) }
    val primary = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.onSurface

    ToolHeader("نقاله", "زاویه را با اسلایدر تنظیم کنید و خط راهنما را با جسم موردنظر مقایسه کنید.")

    Canvas(modifier = Modifier.fillMaxWidth().height(230.dp)) {
        val center = Offset(size.width / 2f, size.height * 0.86f)
        val radius = min(size.width * 0.44f, size.height * 0.72f)

        drawArc(
            color = lineColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawLine(lineColor, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), 2.dp.toPx())

        for (degree in 0..180 step 10) {
            val rad = Math.toRadians(degree.toDouble())
            val outer = Offset(
                center.x + cos(rad).toFloat() * radius,
                center.y - sin(rad).toFloat() * radius,
            )
            val innerRadius = if (degree % 30 == 0) radius - 22.dp.toPx() else radius - 13.dp.toPx()
            val inner = Offset(
                center.x + cos(rad).toFloat() * innerRadius,
                center.y - sin(rad).toFloat() * innerRadius,
            )
            drawLine(lineColor, inner, outer, if (degree % 30 == 0) 2.dp.toPx() else 1.dp.toPx())
        }

        val selectedRad = Math.toRadians(angle.toDouble())
        val selected = Offset(
            center.x + cos(selectedRad).toFloat() * radius,
            center.y - sin(selectedRad).toFloat() * radius,
        )
        drawLine(primary, center, selected, 4.dp.toPx())
        drawCircle(primary, radius = 6.dp.toPx(), center = center)
    }

    Slider(value = angle, onValueChange = { angle = it }, valueRange = 0f..180f)
    ResultCard("زاویه", "${angle.toInt()}°")
}

/** Live pitch/roll meter based on the accelerometer gravity vector. */
@Composable
private fun AngleMeterScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var values by remember { mutableStateOf(floatArrayOf(0f, 0f, SensorManager.GRAVITY_EARTH)) }

    ToolHeader("زاویه‌سنج", if (sensor == null) "شتاب‌سنج روی این دستگاه موجود نیست." else "زاویه شیب دستگاه روی دو محور به‌صورت زنده نمایش داده می‌شود.")

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    values = event.values.copyOf(3)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    if (sensor != null) {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val pitch = Math.toDegrees(atan2(-x.toDouble(), sqrt((y * y + z * z).toDouble())))
        val roll = Math.toDegrees(atan2(y.toDouble(), z.toDouble()))
        val combined = sqrt(pitch * pitch + roll * roll)

        ResultCard("Pitch", "${professionalFormat.format(pitch)}°")
        ResultCard("Roll", "${professionalFormat.format(roll)}°")
        ResultCard(
            "شیب ترکیبی",
            "${professionalFormat.format(combined)}°",
            if (abs(pitch) < 0.8 && abs(roll) < 0.8) "تقریباً تراز" else "دستگاه شیب دارد",
        )
    }
}

/** Relative vibration meter using dynamic acceleration after removing gravity. */
@Composable
private fun VibrometerScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var vibration by remember { mutableFloatStateOf(0f) }
    var peak by remember { mutableFloatStateOf(0f) }

    ToolHeader("لرزش‌سنج", if (sensor == null) "شتاب‌سنج روی این دستگاه موجود نیست." else "شدت لرزش نسبی از تغییر شتاب دستگاه محاسبه می‌شود.")

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt(x * x + y * y + z * z)
                    val dynamic = abs(magnitude - SensorManager.GRAVITY_EARTH)
                    vibration = vibration * 0.82f + dynamic * 0.18f
                    peak = max(peak, dynamic)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    if (sensor != null) {
        val state = when {
            vibration < 0.15f -> "تقریباً ثابت"
            vibration < 0.8f -> "لرزش کم"
            vibration < 2.5f -> "لرزش متوسط"
            else -> "لرزش زیاد"
        }
        ResultCard("شدت لحظه‌ای", "${professionalFormat.format(vibration)} m/s²", state)
        ResultCard("بیشینه", "${professionalFormat.format(peak)} m/s²")
        Button(onClick = { peak = 0f }, modifier = Modifier.fillMaxWidth()) { Text("صفر کردن بیشینه") }
    }
}

@Composable
private fun LocationPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    if (granted) {
        content()
    } else {
        ToolHeader("دسترسی موقعیت مکانی", "این ابزار فقط هنگام استفاده برای دریافت سرعت، مختصات و مسافت به Location نیاز دارد.")
        Button(
            onClick = {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("اجازه دسترسی به موقعیت") }
    }
}

/** Shared location stream using only Android framework APIs. */
@Composable
private fun rememberLiveLocation(onLocation: (Location) -> Unit = {}): Location? {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var location by remember { mutableStateOf<Location?>(null) }

    DisposableEffect(manager) {
        val listener = LocationListener { newLocation ->
            location = newLocation
            onLocation(newLocation)
        }

        fun request(provider: String) {
            runCatching {
                if (manager.isProviderEnabled(provider) &&
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                ) {
                    manager.requestLocationUpdates(provider, 1_000L, 0f, listener, Looper.getMainLooper())
                }
            }
        }

        request(LocationManager.GPS_PROVIDER)
        request(LocationManager.NETWORK_PROVIDER)

        onDispose { runCatching { manager.removeUpdates(listener) } }
    }

    return location
}

@Composable
private fun GpsDashboardScreen() {
    val location = rememberLiveLocation()
    ToolHeader("داشبورد GPS", "سرعت، مختصات، ارتفاع و دقت موقعیت را به‌صورت زنده نمایش می‌دهد.")

    if (location == null) {
        ResultCard("GPS", "در انتظار موقعیت…", "در فضای باز دقت معمولاً بهتر است.")
        return
    }

    val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
    ResultCard("سرعت", "${professionalFormat.format(speedKmh)} km/h", "${professionalFormat.format(location.speed)} m/s")
    ResultCard("مختصات", "${professionalFormat.format(location.latitude)}, ${professionalFormat.format(location.longitude)}")
    ResultCard("ارتفاع", if (location.hasAltitude()) "${professionalFormat.format(location.altitude)} m" else "ناموجود")
    ResultCard("دقت", "±${professionalFormat.format(location.accuracy)} m", location.provider ?: "")
}

@Composable
private fun DistanceTrackerScreen() {
    var tracking by remember { mutableStateOf(false) }
    var totalMeters by remember { mutableFloatStateOf(0f) }
    var lastAccepted by remember { mutableStateOf<Location?>(null) }

    val location = rememberLiveLocation { current ->
        if (!tracking) return@rememberLiveLocation
        if (current.accuracy > 60f) return@rememberLiveLocation

        val previous = lastAccepted
        if (previous != null) {
            val delta = previous.distanceTo(current)
            // Ignore tiny GPS jitter and implausible one-second jumps.
            if (delta in 2f..250f) totalMeters += delta
        }
        lastAccepted = current
    }

    ToolHeader("مسافت‌سنج GPS", "مسافت حرکت را با نقاط GPS جمع می‌کند؛ دقت به کیفیت موقعیت دستگاه وابسته است.")
    ResultCard("مسافت", if (totalMeters >= 1000f) "${professionalFormat.format(totalMeters / 1000f)} km" else "${professionalFormat.format(totalMeters)} m")
    ResultCard("وضعیت GPS", location?.let { "دقت ±${professionalFormat.format(it.accuracy)} m" } ?: "در انتظار موقعیت…")

    ActionRow(
        primaryText = if (tracking) "توقف" else "شروع",
        onPrimary = {
            tracking = !tracking
            if (tracking) lastAccepted = location
        },
        secondaryText = "صفر",
        onSecondary = {
            tracking = false
            totalMeters = 0f
            lastAccepted = null
        },
    )
}

@Composable
private fun MicrophonePermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    if (granted) {
        content()
    } else {
        ToolHeader("دسترسی میکروفون", "صدا فقط برای محاسبه شدت نسبی روی دستگاه خوانده می‌شود و هیچ فایل صوتی ذخیره نمی‌شود.")
        Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.fillMaxWidth()) {
            Text("اجازه دسترسی به میکروفون")
        }
    }
}

/**
 * Relative sound meter using raw PCM RMS. It reports dBFS, not calibrated dB SPL,
 * because Android phones do not expose a universal microphone calibration curve.
 */
@Composable
private fun SoundMeterScreen() {
    val context = LocalContext.current
    var dbFs by remember { mutableFloatStateOf(-90f) }
    var peakDbFs by remember { mutableFloatStateOf(-90f) }
    var available by remember { mutableStateOf(true) }

    ToolHeader("صدا‌سنج", "مقدار به‌صورت dBFS نسبی است؛ برای dB SPL دقیق به میکروفون کالیبره نیاز است.")

    DisposableEffect(Unit) {
        val sampleRate = 44_100
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val bufferSize = max(minBuffer, 4_096)
        val running = AtomicBoolean(true)
        val executor = Executors.newSingleThreadExecutor()
        val mainHandler = Handler(Looper.getMainLooper())

        val recorder = runCatching {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                error("Microphone permission missing")
            }
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
            )
        }.getOrNull()

        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
            available = false
            runCatching { recorder?.release() }
        } else {
            executor.execute {
                val buffer = ShortArray(bufferSize / 2)
                runCatching { recorder.startRecording() }.onFailure {
                    mainHandler.post { available = false }
                    running.set(false)
                }

                while (running.get()) {
                    val count = runCatching { recorder.read(buffer, 0, buffer.size) }.getOrDefault(0)
                    if (count <= 0) continue

                    var sumSquares = 0.0
                    for (index in 0 until count) {
                        val sample = buffer[index].toDouble()
                        sumSquares += sample * sample
                    }
                    val rms = sqrt(sumSquares / count)
                    val value = if (rms <= 1.0) -90.0 else 20.0 * log10(rms / 32768.0)
                    val clipped = value.coerceIn(-90.0, 0.0).toFloat()
                    mainHandler.post {
                        dbFs = dbFs * 0.75f + clipped * 0.25f
                        peakDbFs = max(peakDbFs, clipped)
                    }
                }
            }
        }

        onDispose {
            running.set(false)
            runCatching { if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop() }
            runCatching { recorder?.release() }
            executor.shutdownNow()
        }
    }

    if (!available) {
        ResultCard("صدا‌سنج", "میکروفون برای اندازه‌گیری در دسترس نیست")
    } else {
        val description = when {
            dbFs < -55f -> "بسیار آرام"
            dbFs < -35f -> "آرام"
            dbFs < -18f -> "متوسط"
            else -> "بلند"
        }
        ResultCard("شدت نسبی", "${professionalFormat.format(dbFs)} dBFS", description)
        ResultCard("بیشینه", "${professionalFormat.format(peakDbFs)} dBFS")
        Button(onClick = { peakDbFs = -90f }, modifier = Modifier.fillMaxWidth()) { Text("صفر کردن بیشینه") }
    }
}

/** Converts a Compose color into the ARGB int expected by android.graphics.Paint. */
private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)
