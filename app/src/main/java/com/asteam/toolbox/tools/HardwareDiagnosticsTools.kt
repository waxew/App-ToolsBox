package com.asteam.toolbox.tools

import android.Manifest
import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.asteam.toolbox.R
import com.asteam.toolbox.system.FlashlightTileService
import com.asteam.toolbox.system.ToolboxWidgetProvider
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader

/**
 * On-device diagnostics hub for hardware-backed Toolbox features.
 * It does not fake sensor results: each status is read from Android at runtime.
 */
@Composable
fun HardwareDiagnosticsScreen(onOpenTool: (String) -> Unit) {
    val context = LocalContext.current
    var permissionRevision by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionRevision++ }

    val packageManager = context.packageManager
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

    // Read permission states again after a runtime permission launcher returns.
    @Suppress("UNUSED_VARIABLE")
    val revision = permissionRevision
    val locationGranted = hasAnyPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val microphoneGranted = hasAnyPermission(context, Manifest.permission.RECORD_AUDIO)
    val cameraGranted = hasAnyPermission(context, Manifest.permission.CAMERA)
    val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        hasAnyPermission(context, Manifest.permission.POST_NOTIFICATIONS)

    val gpsEnabled = runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
    val sensorCount = remember { sensorManager.getSensorList(Sensor.TYPE_ALL).size }
    val cameraCount = remember { runCatching { cameraManager.cameraIdList.size }.getOrDefault(0) }
    val hasMicrophone = packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) && cameraCount > 0
    val canPinWidget = appWidgetManager.isRequestPinAppWidgetSupported

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ToolHeader(
            "مرکز تست سخت‌افزار",
            "وضعیت قابلیت‌های وابسته به سخت‌افزار و مجوزهای Android را روی همین گوشی بررسی کنید.",
        )

        ResultCard(
            title = "خلاصه دستگاه",
            value = "$sensorCount سنسور • $cameraCount دوربین",
            details = "GPS: ${if (gpsEnabled) "فعال" else "خاموش"} | میکروفون: ${if (hasMicrophone) "موجود" else "ناموجود"}",
        )

        HardwareCheckCard(
            title = "GPS و موقعیت مکانی",
            status = when {
                !gpsEnabled -> "GPS خاموش است"
                !locationGranted -> "GPS فعال است؛ مجوز موقعیت لازم است"
                else -> "آماده تست"
            },
            ready = gpsEnabled && locationGranted,
            primaryText = if (locationGranted) "باز کردن داشبورد GPS" else "دادن مجوز موقعیت",
            onPrimary = {
                if (locationGranted) onOpenTool("gps_dashboard")
                else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            secondaryText = if (!gpsEnabled) "تنظیمات GPS" else null,
            onSecondary = if (!gpsEnabled) ({ openSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS) }) else null,
        )

        HardwareCheckCard(
            title = "میکروفون",
            status = when {
                !hasMicrophone -> "این دستگاه میکروفون گزارش نکرده است"
                !microphoneGranted -> "مجوز میکروفون داده نشده"
                else -> "آماده تست زنده"
            },
            ready = hasMicrophone && microphoneGranted,
            primaryText = if (microphoneGranted) "باز کردن صداسنج" else "دادن مجوز میکروفون",
            onPrimary = {
                if (microphoneGranted) onOpenTool("sound_meter")
                else permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            },
        )

        HardwareCheckCard(
            title = "سنسورها",
            status = if (sensorCount > 0) "$sensorCount سنسور توسط Android شناسایی شده" else "سنسوری گزارش نشده",
            ready = sensorCount > 0,
            primaryText = "مشاهده فهرست سنسورها",
            onPrimary = { onOpenTool("sensors") },
        )

        HardwareCheckCard(
            title = "دوربین",
            status = when {
                !hasCamera -> "دوربین قابل استفاده پیدا نشد"
                !cameraGranted -> "$cameraCount دوربین موجود؛ مجوز لازم است"
                else -> "$cameraCount دوربین آماده استفاده"
            },
            ready = hasCamera && cameraGranted,
            primaryText = if (cameraGranted) "تست دوربین و ذره‌بین" else "دادن مجوز دوربین",
            onPrimary = {
                if (cameraGranted) onOpenTool("magnifier")
                else permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            },
        )

        HardwareCheckCard(
            title = "QR / Barcode Scanner",
            status = if (hasCamera && cameraGranted) "CameraX و اسکنر آماده اجرا هستند" else "برای تست Scanner ابتدا دوربین را فعال کنید",
            ready = hasCamera && cameraGranted,
            primaryText = "باز کردن QR Scanner",
            onPrimary = { onOpenTool("qr_scanner") },
            secondaryText = "Barcode Scanner",
            onSecondary = { onOpenTool("barcode_scanner") },
        )

        HardwareCheckCard(
            title = "Home Screen Widget",
            status = if (canPinWidget) "لانچر از Pin Widget پشتیبانی می‌کند" else "افزودن Widget باید از منوی لانچر انجام شود",
            ready = canPinWidget,
            primaryText = if (canPinWidget) "افزودن Widget" else "راهنمای Widget",
            onPrimary = {
                if (canPinWidget) {
                    appWidgetManager.requestPinAppWidget(
                        ComponentName(context, ToolboxWidgetProvider::class.java),
                        null,
                        null,
                    )
                } else {
                    openSettings(context, Settings.ACTION_SETTINGS)
                }
            },
        )

        HardwareCheckCard(
            title = "Quick Settings Tile",
            status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "Android امکان درخواست مستقیم افزودن Tile را می‌دهد"
            } else {
                "Tile را از پنل ویرایش Quick Settings اضافه کنید"
            },
            ready = true,
            primaryText = "افزودن/مدیریت Tile چراغ‌قوه",
            onPrimary = { requestFlashlightTile(context) },
        )

        HardwareCheckCard(
            title = "Reminder و اعلان",
            status = if (notificationsGranted) "اعلان‌ها آماده‌اند" else "مجوز اعلان داده نشده",
            ready = notificationsGranted,
            primaryText = if (notificationsGranted) "باز کردن یادآور" else "دادن مجوز اعلان",
            onPrimary = {
                if (notificationsGranted) {
                    onOpenTool("local_reminder")
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            },
        )
    }
}

@Composable
private fun HardwareCheckCard(
    title: String,
    status: String,
    ready: Boolean,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ready) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPrimary, modifier = Modifier.weight(1f)) { Text(primaryText) }
                if (secondaryText != null && onSecondary != null) {
                    OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f)) { Text(secondaryText) }
                }
            }
        }
    }
}

private fun hasAnyPermission(context: Context, vararg permissions: String): Boolean =
    permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

private fun openSettings(context: Context, action: String) {
    runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private fun requestFlashlightTile(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val statusBarManager = context.getSystemService(StatusBarManager::class.java)
        runCatching {
            statusBarManager.requestAddTileService(
                ComponentName(context, FlashlightTileService::class.java),
                "چراغ‌قوه جعبه ابزار",
                Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
                context.mainExecutor,
            ) { }
        }
    } else {
        openSettings(context, Settings.ACTION_QUICK_SETTINGS)
    }
}
