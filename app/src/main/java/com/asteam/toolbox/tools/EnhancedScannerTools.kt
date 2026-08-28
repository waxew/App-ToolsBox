package com.asteam.toolbox.tools

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Final v1.1 scanner flow.
 *
 * The analyzer accepts one code, pauses immediately, stores that result once,
 * plays a short local feedback signal, and waits for an explicit "scan again"
 * action. This prevents repeated reads of the same QR/barcode while it remains
 * visible in front of the camera.
 */
@Composable
fun EnhancedScannerToolScreen(onlyQr: Boolean, preferences: UserPreferences) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = it
    }

    if (!permissionGranted) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ToolHeader("دسترسی دوربین", "برای اسکن زنده فقط هنگام استفاده از این ابزار به دوربین نیاز است.")
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("اجازه دسترسی به دوربین") }
        }
        return
    }

    EnhancedScannerContent(onlyQr = onlyQr, preferences = preferences)
}

@Composable
private fun EnhancedScannerContent(onlyQr: Boolean, preferences: UserPreferences) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var resultText by remember { mutableStateOf("") }
    var resultFormat by remember { mutableStateOf("") }
    var resultType by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("در انتظار اسکن…") }
    var paused by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }

    // Atomic gate is shared with the camera analyzer thread and guarantees that
    // only one result can be accepted before the user explicitly resumes.
    val scanGate = remember { AtomicBoolean(true) }

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

    fun resetScanner(clearResult: Boolean = true) {
        scanGate.set(true)
        paused = false
        status = "در انتظار اسکن…"
        if (clearResult) {
            resultText = ""
            resultFormat = ""
            resultType = ""
        }
    }

    fun acceptBarcode(barcode: Barcode) {
        val value = barcode.rawValue.orEmpty().trim()
        if (value.isBlank()) return
        if (!scanGate.compareAndSet(true, false)) return

        val format = enhancedBarcodeFormatName(barcode.format)
        resultText = value
        resultFormat = format
        resultType = enhancedBarcodeValueTypeName(barcode.valueType, value)
        status = "اسکن موفق؛ برای خواندن کد بعدی «اسکن مجدد» را بزنید."
        paused = true

        // Duplicate values are de-duplicated by UserPreferences before storage.
        preferences.addScanHistory(value, format)
        performScanFeedback(context)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        resetScanner(clearResult = true)
        status = "در حال بررسی تصویر…"
        runCatching { InputImage.fromFilePath(context, uri) }
            .onSuccess { image ->
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        if (barcode != null) acceptBarcode(barcode)
                        else status = "QR یا Barcode قابل خواندن در این تصویر پیدا نشد."
                    }
                    .addOnFailureListener { status = "خواندن تصویر ناموفق بود." }
            }
            .onFailure { status = "باز کردن تصویر ناموفق بود." }
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
                // When paused, frames are discarded immediately so ML Kit does
                // not waste battery processing the same visible code repeatedly.
                if (!scanGate.get()) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(input)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.let(::acceptBarcode)
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }

            runCatching {
                provider.unbindAll()
                boundCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }.onFailure { status = "دوربین برای اسکن در دسترس نیست." }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { boundCamera?.cameraControl?.enableTorch(false) }
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
            if (onlyQr) "اسکن QR" else "اسکن QR و بارکد",
            "پس از تشخیص، اسکن به‌صورت خودکار متوقف می‌شود تا نتیجه تکراری ثبت نشود.",
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(340.dp),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(340.dp))
                Box(
                    modifier = Modifier
                        .size(width = 230.dp, height = 180.dp)
                        .border(
                            width = 2.dp,
                            color = if (paused) MaterialTheme.colorScheme.primary else Color.White,
                            shape = MaterialTheme.shapes.medium,
                        ),
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val camera = boundCamera ?: return@Button
                    if (!camera.cameraInfo.hasFlashUnit()) return@Button
                    torchEnabled = !torchEnabled
                    camera.cameraControl.enableTorch(torchEnabled)
                },
                enabled = boundCamera?.cameraInfo?.hasFlashUnit() == true,
                modifier = Modifier.weight(1f),
            ) { Text(if (torchEnabled) "خاموش کردن فلش" else "روشن کردن فلش") }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
            ) { Text("اسکن از گالری") }
        }

        if (paused) {
            Button(
                onClick = { resetScanner(clearResult = true) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("اسکن مجدد") }
        }

        ResultCard(
            title = "وضعیت",
            value = status,
            details = if (resultText.isBlank()) null else "$resultFormat | $resultType",
        )

        if (resultText.isNotBlank()) {
            ResultCard("نتیجه اسکن", resultText)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("scan-result", resultText)) },
                    modifier = Modifier.weight(1f),
                ) { Text("کپی") }

                Button(
                    onClick = { shareScannedText(context, resultText) },
                    modifier = Modifier.weight(1f),
                ) { Text("اشتراک") }
            }

            enhancedResolveScanAction(resultText)?.let { resolved ->
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(resolved.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(resolved.label) }
            }
        }
    }
}

/** Produces a short local beep and vibration after one successful read. */
private fun performScanFeedback(context: Context) {
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 220L)
    }

    runCatching {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(70L)
        }
    }
}

private fun shareScannedText(context: Context, value: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, value)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک نتیجه اسکن"))
}

private data class EnhancedScanAction(val label: String, val intent: Intent)

private fun enhancedResolveScanAction(value: String): EnhancedScanAction? {
    val trimmed = value.trim()
    return when {
        trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true) ->
            EnhancedScanAction("باز کردن لینک", Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)))
        trimmed.startsWith("mailto:", true) ->
            EnhancedScanAction("ارسال ایمیل", Intent(Intent.ACTION_SENDTO, Uri.parse(trimmed)))
        trimmed.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) ->
            EnhancedScanAction("ارسال ایمیل", Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")))
        trimmed.startsWith("tel:", true) ->
            EnhancedScanAction("باز کردن شماره‌گیر", Intent(Intent.ACTION_DIAL, Uri.parse(trimmed)))
        trimmed.matches(Regex("^[+0-9][0-9 ()-]{5,}$")) ->
            EnhancedScanAction("باز کردن شماره‌گیر", Intent(Intent.ACTION_DIAL, Uri.parse("tel:${trimmed.replace(" ", "")}")))
        trimmed.startsWith("geo:", true) ->
            EnhancedScanAction("باز کردن نقشه", Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)))
        trimmed.startsWith("WIFI:", true) ->
            EnhancedScanAction("تنظیمات Wi-Fi", Intent(Settings.ACTION_WIFI_SETTINGS))
        else -> null
    }
}

private fun enhancedBarcodeValueTypeName(valueType: Int, rawValue: String): String = when (valueType) {
    Barcode.TYPE_URL -> "لینک"
    Barcode.TYPE_EMAIL -> "ایمیل"
    Barcode.TYPE_PHONE -> "شماره تلفن"
    Barcode.TYPE_SMS -> "پیامک"
    Barcode.TYPE_WIFI -> "Wi-Fi"
    Barcode.TYPE_GEO -> "موقعیت مکانی"
    Barcode.TYPE_CONTACT_INFO -> "اطلاعات تماس"
    Barcode.TYPE_CALENDAR_EVENT -> "رویداد تقویم"
    Barcode.TYPE_DRIVER_LICENSE -> "گواهینامه"
    Barcode.TYPE_ISBN -> "ISBN"
    Barcode.TYPE_PRODUCT -> "محصول"
    Barcode.TYPE_TEXT -> if (rawValue.startsWith("http", true)) "لینک" else "متن"
    else -> "داده"
}

private fun enhancedBarcodeFormatName(format: Int): String = when (format) {
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
