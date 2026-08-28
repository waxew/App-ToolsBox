package com.asteam.toolbox.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.components.ActionRow
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.random.Random

@Composable
fun EverydayToolScreen(toolId: String, title: String, preferences: UserPreferences) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "stopwatch" -> StopwatchScreen()
            "timer" -> TimerScreen()
            "age" -> AgeScreen()
            "date_diff" -> DateDifferenceScreen()
            "random" -> RandomNumberScreen()
            "dice" -> DiceScreen()
            "coin" -> CoinScreen()
            "password" -> PasswordScreen()
            "base64" -> Base64Screen()
            "sha256" -> Sha256Screen()
            "text_stats" -> TextStatsScreen()
            "number_base" -> NumberBaseScreen()
            "clipboard" -> ClipboardScreen()
            "counter" -> CounterScreen(preferences)
            "qr" -> QrScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var accumulated by remember { mutableLongStateOf(0L) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var shown by remember { mutableLongStateOf(0L) }
    LaunchedEffect(running, accumulated) {
        if (running) while (true) { shown = accumulated + (SystemClock.elapsedRealtime() - startedAt); delay(50) }
        else shown = accumulated
    }
    ToolHeader("کرنومتر")
    ResultCard("زمان", formatMillis(shown))
    ActionRow(
        primaryText = if (running) "توقف" else "شروع",
        onPrimary = {
            if (running) { accumulated += SystemClock.elapsedRealtime() - startedAt; running = false }
            else { startedAt = SystemClock.elapsedRealtime(); running = true }
        },
        secondaryText = "صفر",
        onSecondary = { running = false; accumulated = 0L; shown = 0L },
    )
}

@Composable
private fun TimerScreen() {
    var secondsInput by remember { mutableStateOf("60") }
    var remaining by remember { mutableLongStateOf(60_000L) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running && remaining > 0L) {
            delay(100L); remaining = max(0L, remaining - 100L); if (remaining == 0L) running = false
        }
    }
    ToolHeader("تایمر", "زمان را بر حسب ثانیه وارد کنید")
    NumberField("ثانیه", secondsInput, { secondsInput = it })
    ResultCard("باقی‌مانده", formatMillis(remaining))
    ActionRow(
        primaryText = if (running) "توقف" else "شروع",
        onPrimary = {
            if (remaining == 0L || (!running && remaining == 60_000L)) remaining = ((secondsInput.toDoubleOrNull() ?: 60.0) * 1000).toLong()
            running = !running
        },
        secondaryText = "تنظیم مجدد",
        onSecondary = { running = false; remaining = ((secondsInput.toDoubleOrNull() ?: 60.0) * 1000).toLong() },
    )
}

@Composable
private fun AgeScreen() {
    var birth by remember { mutableStateOf("2000-01-01") }
    val result = remember(birth) {
        runCatching {
            val date = LocalDate.parse(birth); val today = LocalDate.now(); require(!date.isAfter(today)); val p = Period.between(date, today)
            "${p.years} سال، ${p.months} ماه، ${p.days} روز"
        }.getOrElse { "تاریخ را به شکل 2000-01-01 وارد کنید" }
    }
    ToolHeader("محاسبه سن", "فرمت تاریخ: YYYY-MM-DD")
    OutlinedTextField(birth, { birth = it }, label = { Text("تاریخ تولد میلادی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("سن", result)
}

@Composable
private fun DateDifferenceScreen() {
    var first by remember { mutableStateOf("2026-01-01") }; var second by remember { mutableStateOf("2026-12-31") }
    val result = remember(first, second) { runCatching { kotlin.math.abs(ChronoUnit.DAYS.between(LocalDate.parse(first), LocalDate.parse(second))) }.getOrNull() }
    ToolHeader("اختلاف تاریخ", "دو تاریخ میلادی با فرمت YYYY-MM-DD")
    OutlinedTextField(first, { first = it }, label = { Text("تاریخ اول") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(second, { second = it }, label = { Text("تاریخ دوم") }, modifier = Modifier.fillMaxWidth())
    ResultCard("فاصله", result?.let { "$it روز" } ?: "تاریخ نامعتبر")
}

@Composable
private fun RandomNumberScreen() {
    var minValue by remember { mutableStateOf("1") }; var maxValue by remember { mutableStateOf("100") }; var result by remember { mutableStateOf("—") }
    ToolHeader("عدد تصادفی")
    NumberField("حداقل", minValue, { minValue = it }); NumberField("حداکثر", maxValue, { maxValue = it })
    Button(onClick = {
        val a = minValue.toLongOrNull() ?: 1L; val b = maxValue.toLongOrNull() ?: 100L; val low = minOf(a, b); val high = maxOf(a, b)
        result = if (low == high) low.toString() else if (high < Long.MAX_VALUE) Random.nextLong(low, high + 1).toString() else Random.nextLong(low, high).toString()
    }, modifier = Modifier.fillMaxWidth()) { Text("انتخاب") }
    ResultCard("نتیجه", result)
}

@Composable
private fun DiceScreen() {
    var value by remember { mutableIntStateOf(1) }; ToolHeader("تاس"); ResultCard("تاس", value.toString())
    Button(onClick = { value = Random.nextInt(1, 7) }, modifier = Modifier.fillMaxWidth()) { Text("پرتاب") }
}

@Composable
private fun CoinScreen() {
    var value by remember { mutableStateOf("شیر") }; ToolHeader("شیر یا خط"); ResultCard("نتیجه", value)
    Button(onClick = { value = if (Random.nextBoolean()) "شیر" else "خط" }, modifier = Modifier.fillMaxWidth()) { Text("پرتاب") }
}

@Composable
private fun PasswordScreen() {
    var length by remember { mutableStateOf("16") }; var symbols by remember { mutableStateOf(true) }; var password by remember { mutableStateOf("") }
    ToolHeader("رمزساز", "رمز روی دستگاه ساخته می‌شود و جایی ارسال نمی‌شود")
    NumberField("طول رمز (۸ تا ۶۴)", length, { length = it })
    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(symbols, { symbols = it }); Text("استفاده از نشانه‌ها") }
    Button(onClick = {
        val count = (length.toIntOrNull() ?: 16).coerceIn(8, 64)
        val chars = buildString { append("ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"); if (symbols) append("!@#$%&*+-_=?") }
        password = (1..count).joinToString("") { chars.random().toString() }
    }, modifier = Modifier.fillMaxWidth()) { Text("ساخت رمز") }
    ResultCard("رمز", password.ifBlank { "دکمه ساخت رمز را بزنید" })
}

@Composable
private fun Base64Screen() {
    var input by remember { mutableStateOf("App-ToolsBox") }; var output by remember { mutableStateOf("") }
    ToolHeader("Base64")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    ActionRow("Encode", { output = Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }, "Decode", {
        output = runCatching { String(Base64.decode(input, Base64.DEFAULT), Charsets.UTF_8) }.getOrElse { "ورودی Base64 معتبر نیست" }
    })
    ResultCard("خروجی", output.ifBlank { "—" })
}

@Composable
private fun Sha256Screen() {
    var input by remember { mutableStateOf("App-ToolsBox") }
    val hash = remember(input) { MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) } }
    ToolHeader("SHA-256", "هش یک‌طرفه است")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    ResultCard("SHA-256", hash)
}

@Composable
private fun TextStatsScreen() {
    var input by remember { mutableStateOf("") }
    val words = input.trim().takeIf { it.isNotEmpty() }?.split(Regex("\\s+"))?.size ?: 0; val lines = if (input.isEmpty()) 0 else input.lines().size
    ToolHeader("شمارش متن"); OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth(), minLines = 6)
    ResultCard("آمار", "${input.length} کاراکتر", "$words کلمه | $lines خط")
}

@Composable
private fun NumberBaseScreen() {
    var input by remember { mutableStateOf("255") }; val decimal = input.toLongOrNull()
    ToolHeader("مبنای عدد", "ورودی این بخش دهدهی است")
    OutlinedTextField(input, { input = it }, label = { Text("عدد دهدهی") }, modifier = Modifier.fillMaxWidth())
    ResultCard("تبدیل", decimal?.let { "BIN ${it.toString(2)}" } ?: "نامعتبر", decimal?.let { "HEX ${it.toString(16).uppercase()} | OCT ${it.toString(8)}" })
}

@Composable
private fun ClipboardScreen() {
    val context = LocalContext.current; val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    var text by remember { mutableStateOf(clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()) }
    ToolHeader("کلیپ‌بورد", "متن فقط با لمس دکمه خوانده یا نوشته می‌شود")
    OutlinedTextField(text, { text = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
    ActionRow("کپی", { clipboard.setPrimaryClip(ClipData.newPlainText("App-ToolsBox", text)) }, "خواندن", { text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty() })
}

@Composable
private fun CounterScreen(preferences: UserPreferences) {
    var count by remember { mutableIntStateOf(preferences.counter) }
    fun save(value: Int) { count = value; preferences.counter = value }
    ToolHeader("شمارنده", "مقدار پس از بستن برنامه حفظ می‌شود"); ResultCard("مقدار", count.toString())
    ActionRow("+1", { save(count + 1) }, "−1", { save(count - 1) })
    Button(onClick = { save(0) }, modifier = Modifier.fillMaxWidth()) { Text("صفر") }
}

@Composable
private fun QrScreen() {
    var text by remember { mutableStateOf("https://github.com/waxew/App-ToolsBox") }; var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    ToolHeader("QR ساز", "QR کاملاً آفلاین ساخته می‌شود")
    OutlinedTextField(text, { text = it }, label = { Text("متن یا لینک") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    Button(onClick = { if (text.isNotBlank()) bitmap = runCatching { createQrBitmap(text, 700) }.getOrNull() }, modifier = Modifier.fillMaxWidth()) { Text("ساخت QR") }
    bitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxWidth()) }
}

private fun createQrBitmap(text: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size); val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) for (y in 0 until size) bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    return bitmap
}

private fun formatMillis(value: Long): String {
    val h = value / 10L; val hundredths = h % 100L; val totalSeconds = h / 100L; val seconds = totalSeconds % 60L; val minutes = (totalSeconds / 60L) % 60L; val hours = totalSeconds / 3600L
    return "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, hundredths)
}
