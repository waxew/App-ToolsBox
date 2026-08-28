package com.asteam.toolbox.tools

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

/**
 * Advanced QR generator introduced in v1.1.0.
 *
 * All payload construction and QR rendering are local. No QR content is sent
 * to a server. The user can create plain text/link, Wi-Fi, contact, phone, SMS
 * and email QR codes, then copy, save or share them.
 */
@Composable
fun AdvancedQrScreen() {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var type by remember { mutableStateOf(QrPayloadType.TEXT) }
    var text by remember { mutableStateOf("https://github.com/waxew/App-ToolsBox") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }
    var wifiHidden by remember { mutableStateOf(false) }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsMessage by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedPayload by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    fun currentPayload(): String = when (type) {
        QrPayloadType.TEXT -> text.trim()
        QrPayloadType.WIFI -> buildWifiPayload(wifiSsid, wifiPassword, wifiSecurity, wifiHidden)
        QrPayloadType.CONTACT -> buildVCardPayload(contactName, contactPhone, contactEmail)
        QrPayloadType.PHONE -> phone.trim().takeIf { it.isNotBlank() }?.let { "tel:$it" }.orEmpty()
        QrPayloadType.SMS -> buildSmsPayload(smsNumber, smsMessage)
        QrPayloadType.EMAIL -> buildEmailPayload(emailAddress, emailSubject, emailBody)
    }

    fun generate() {
        val payload = currentPayload()
        if (payload.isBlank()) {
            status = "اطلاعات لازم را وارد کنید."
            bitmap = null
            generatedPayload = ""
            return
        }
        bitmap = runCatching { createQrBitmapAdvanced(payload, 900) }.getOrNull()
        generatedPayload = if (bitmap != null) payload else ""
        status = if (bitmap != null) "QR آماده است." else "ساخت QR ناموفق بود."
    }

    val legacySavePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val image = bitmap
        if (granted && image != null) {
            status = saveQrToGallery(context, image).getOrElse { "ذخیره QR ناموفق بود." }
        } else if (!granted) {
            status = "برای ذخیره در گالری در Android قدیمی، مجوز ذخیره‌سازی لازم است."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ToolHeader("QR ساز حرفه‌ای", "ساخت، ذخیره و اشتراک‌گذاری QR کاملاً روی دستگاه انجام می‌شود.")

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QrPayloadType.entries) { item ->
                FilterChip(
                    selected = type == item,
                    onClick = {
                        type = item
                        bitmap = null
                        generatedPayload = ""
                        status = ""
                    },
                    label = { Text(item.title) },
                )
            }
        }

        when (type) {
            QrPayloadType.TEXT -> {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("متن یا لینک") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }

            QrPayloadType.WIFI -> {
                OutlinedTextField(wifiSsid, { wifiSsid = it }, label = { Text("نام Wi-Fi / SSID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(wifiPassword, { wifiPassword = it }, label = { Text("رمز Wi-Fi") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WPA", "WEP", "nopass").forEach { security ->
                        FilterChip(
                            selected = wifiSecurity == security,
                            onClick = { wifiSecurity = security },
                            label = { Text(if (security == "nopass") "بدون رمز" else security) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wifiHidden, onCheckedChange = { wifiHidden = it })
                    Text("شبکه مخفی است")
                }
            }

            QrPayloadType.CONTACT -> {
                OutlinedTextField(contactName, { contactName = it }, label = { Text("نام مخاطب") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("شماره تلفن") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(contactEmail, { contactEmail = it }, label = { Text("ایمیل") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            QrPayloadType.PHONE -> {
                OutlinedTextField(phone, { phone = it }, label = { Text("شماره تلفن") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            QrPayloadType.SMS -> {
                OutlinedTextField(smsNumber, { smsNumber = it }, label = { Text("شماره گیرنده") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(smsMessage, { smsMessage = it }, label = { Text("متن پیامک") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }

            QrPayloadType.EMAIL -> {
                OutlinedTextField(emailAddress, { emailAddress = it }, label = { Text("ایمیل گیرنده") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(emailSubject, { emailSubject = it }, label = { Text("موضوع") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(emailBody, { emailBody = it }, label = { Text("متن ایمیل") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        }

        Button(onClick = ::generate, modifier = Modifier.fillMaxWidth()) { Text("ساخت QR") }

        bitmap?.let { image ->
            Image(bitmap = image.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ) {
                            legacySavePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            status = saveQrToGallery(context, image).getOrElse { "ذخیره QR ناموفق بود." }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("ذخیره PNG") }

                Button(
                    onClick = {
                        status = shareQrImage(context, image, generatedPayload).getOrElse { "اشتراک‌گذاری ناموفق بود." }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("اشتراک QR") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        clipboard.setPrimaryClip(ClipData.newPlainText("qr-payload", generatedPayload))
                        status = "محتوای QR کپی شد."
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("کپی محتوا") }

                Button(
                    onClick = {
                        shareText(context, generatedPayload)
                        status = "پنجره اشتراک متن باز شد."
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("اشتراک متن") }
            }
        }

        if (status.isNotBlank()) ResultCard("وضعیت", status)
    }
}

private enum class QrPayloadType(val title: String) {
    TEXT("متن/لینک"),
    WIFI("Wi-Fi"),
    CONTACT("مخاطب"),
    PHONE("تلفن"),
    SMS("SMS"),
    EMAIL("ایمیل"),
}

private fun buildWifiPayload(ssid: String, password: String, security: String, hidden: Boolean): String {
    if (ssid.isBlank()) return ""
    val escapedSsid = escapeQrField(ssid)
    val escapedPassword = escapeQrField(password)
    val passwordPart = if (security == "nopass") "" else "P:$escapedPassword;"
    return "WIFI:T:$security;S:$escapedSsid;$passwordPart" + "H:${if (hidden) "true" else "false"};;"
}

private fun buildVCardPayload(name: String, phone: String, email: String): String {
    if (name.isBlank() && phone.isBlank() && email.isBlank()) return ""
    return buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        if (name.isNotBlank()) appendLine("FN:${escapeVCard(name)}")
        if (phone.isNotBlank()) appendLine("TEL:${escapeVCard(phone)}")
        if (email.isNotBlank()) appendLine("EMAIL:${escapeVCard(email)}")
        append("END:VCARD")
    }
}

private fun buildSmsPayload(number: String, message: String): String {
    if (number.isBlank()) return ""
    return "SMSTO:${number.trim()}:${message.trim()}"
}

private fun buildEmailPayload(address: String, subject: String, body: String): String {
    if (address.isBlank()) return ""
    return "MATMSG:TO:${escapeQrField(address)};SUB:${escapeQrField(subject)};BODY:${escapeQrField(body)};;"
}

private fun escapeQrField(value: String): String =
    value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace(":", "\\:")

private fun escapeVCard(value: String): String =
    value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")

private fun createQrBitmapAdvanced(text: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

private fun saveQrToGallery(context: Context, bitmap: Bitmap): Result<String> = runCatching {
    val fileName = "App-ToolsBox-QR-${System.currentTimeMillis()}.png"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/App-ToolsBox")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("MediaStore insert failed")
        resolver.openOutputStream(uri)?.use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        } ?: error("MediaStore output stream failed")
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    } else {
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val directory = File(pictures, "App-ToolsBox").apply { mkdirs() }
        val file = File(directory, fileName)
        FileOutputStream(file).use { stream -> check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
    }
    "QR در پوشه Pictures/App-ToolsBox ذخیره شد."
}

private fun shareQrImage(context: Context, bitmap: Bitmap, payload: String): Result<String> = runCatching {
    val directory = File(context.cacheDir, "shared_qr").apply { mkdirs() }
    val file = File(directory, "qr-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { stream -> check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, payload)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک QR"))
    "پنجره اشتراک QR باز شد."
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک محتوا"))
}
