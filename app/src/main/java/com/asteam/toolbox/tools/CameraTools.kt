package com.asteam.toolbox.tools

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.asteam.toolbox.data.ScanHistoryItem
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.components.ActionRow
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/** Camera-based tools introduced in v1.1.0. */
@Composable
fun CameraToolScreen(toolId: String, title: String, preferences: UserPreferences) {
    when (toolId) {
        "qr_scanner" -> ScannerScreen(onlyQr = true, preferences = preferences)
        "barcode_scanner" -> ScannerScreen(onlyQr = false, preferences = preferences)
        "scan_history" -> ScanHistoryScreen(preferences)
        "magnifier" -> CameraPreviewScreen(title = "ذره‌بین", useFrontCamera = false, enableZoomControls = true)
        "mirror" -> CameraPreviewScreen(title = "آینه", useFrontCamera = true, enableZoomControls = false)
        else -> ToolHeader(title)
    }
}

@Composable
private fun CameraPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    if (granted) {
        content()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ToolHeader("دسترسی دوربین", "این ابزار فقط هنگام استفاده به دوربین نیاز دارد.")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth()) {
                Text("اجازه دسترسی به دوربین")
            }
        }
    }
}

@Composable
private fun ScannerScreen(onlyQr: Boolean, preferences: UserPreferences) {
    CameraPermissionGate {
        ScannerCameraContent(onlyQr = onlyQr, preferences = preferences)
    }
}

@Composable
private fun ScannerCameraContent(onlyQr: Boolean, preferences: UserPreferences) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var resultText by remember { mutableStateOf("") }
    var resultFormat by remember { mutableStateOf("") }
    var lastSavedValue by remember { mutableStateOf("") }
    var lastSavedAt by remember { mutableStateOf(0L) }
    val scanner = remember(onlyQr) {
        val options = if (onlyQr) {
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        } else {
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        }
        BarcodeScanning.getClient(options)
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, scanner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = runCatching { providerFuture.get() }.getOrNull() ?: return@Runnable
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() } ?: return@addOnSuccessListener
                        val value = barcode.rawValue.orEmpty()
                        val format = barcodeFormatName(barcode.format)
                        resultText = value
                        resultFormat = format

                        val now = System.currentTimeMillis()
                        if (value != lastSavedValue || now - lastSavedAt > 2_000L) {
                            preferences.addScanHistory(value, format, now)
                            lastSavedValue = value
                            lastSavedAt = now
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }

            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            scanner.close()
            analysisExecutor.shutdown()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader(
            if (onlyQr) "اسکن QR" else "اسکن بارکد",
            if (onlyQr) "QR را داخل کادر دوربین قرار دهید." else "QR و بارکدهای رایج به‌صورت زنده خوانده می‌شوند.",
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(340.dp))
        }
        ResultCard(
            title = "نتیجه اسکن",
            value = resultText.ifBlank { "در انتظار اسکن…" },
            details = resultFormat.takeIf { it.isNotBlank() },
        )
        if (resultText.isNotBlank()) {
            val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
            Button(
                onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("scan-result", resultText)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("کپی نتیجه") }
        }
    }
}

@Composable
private fun ScanHistoryScreen(preferences: UserPreferences) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    var revision by remember { mutableIntStateOf(0) }
    val items = remember(revision) { preferences.scanHistory() }
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ToolHeader("تاریخچه اسکن", "حداکثر ۱۰۰ نتیجه اخیر فقط روی همین دستگاه نگه‌داری می‌شود.")
        if (items.isEmpty()) {
            ResultCard("تاریخچه", "هنوز چیزی اسکن نشده است")
        } else {
            ActionRow(
                primaryText = "پاک کردن تاریخچه",
                onPrimary = { preferences.clearScanHistory(); revision++ },
            )
            items.forEach { item ->
                ScanHistoryCard(item = item, formatter = formatter, onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("scan-history", item.value))
                })
            }
        }
    }
}

@Composable
private fun ScanHistoryCard(item: ScanHistoryItem, formatter: SimpleDateFormat, onCopy: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.format, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(item.value, style = MaterialTheme.typography.bodyLarge)
            Text(formatter.format(Date(item.scannedAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onCopy, modifier = Modifier.fillMaxWidth()) { Text("کپی") }
        }
    }
}

@Composable
private fun CameraPreviewScreen(title: String, useFrontCamera: Boolean, enableZoomControls: Boolean) {
    CameraPermissionGate {
        CameraPreviewContent(title = title, useFrontCamera = useFrontCamera, enableZoomControls = enableZoomControls)
    }
}

@Composable
private fun CameraPreviewContent(title: String, useFrontCamera: Boolean, enableZoomControls: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var zoom by remember { mutableStateOf(1f) }
    var boundCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    DisposableEffect(lifecycleOwner, useFrontCamera) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = runCatching { providerFuture.get() }.getOrNull() ?: return@Runnable
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            runCatching {
                provider.unbindAll()
                boundCamera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            boundCamera = null
        }
    }

    LaunchedEffect(zoom, boundCamera) {
        boundCamera?.cameraInfo?.zoomState?.value?.let { zoomState ->
            val safeZoom = zoom.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
            boundCamera?.cameraControl?.setZoomRatio(safeZoom)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader(
            title,
            if (useFrontCamera) "نمای زنده دوربین جلو؛ پردازش و ذخیره‌ای انجام نمی‌شود." else "نمای زنده دوربین عقب با کنترل بزرگ‌نمایی.",
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(420.dp))
            }
        }
        if (enableZoomControls) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1f, 2f, 4f).forEach { ratio ->
                    Button(onClick = { zoom = ratio }, modifier = Modifier.weight(1f)) { Text("${ratio.toInt()}×") }
                }
            }
        }
    }
}

private fun barcodeFormatName(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR Code"
    Barcode.FORMAT_AZTEC -> "Aztec"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_EAN_8 -> "EAN-8"
    Barcode.FORMAT_EAN_13 -> "EAN-13"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_UPC_A -> "UPC-A"
    Barcode.FORMAT_UPC_E -> "UPC-E"
    else -> "Barcode"
}
