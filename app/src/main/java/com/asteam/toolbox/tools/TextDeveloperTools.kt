package com.asteam.toolbox.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

/** Text/developer utility family introduced in v1.4.0. */
@Composable
fun TextDeveloperToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (toolId) {
            "digit_converter" -> DigitConverterScreen()
            "sort_lines" -> LineTransformScreen(mode = "sort")
            "dedupe_lines" -> LineTransformScreen(mode = "dedupe")
            "case_converter" -> CaseConverterScreen()
            "json_formatter" -> JsonFormatterScreen()
            "url_codec" -> UrlCodecScreen()
            "html_codec" -> HtmlCodecScreen()
            "uuid" -> UuidScreen()
            "hash_suite" -> HashSuiteScreen()
            "text_compare" -> TextCompareScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun CopyableTextResult(title: String, value: String) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    ResultCard(title, value.ifBlank { "—" })
    Button(
        onClick = { clipboard.setPrimaryClip(ClipData.newPlainText(title, value)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = value.isNotBlank(),
    ) { Text("کپی نتیجه") }
}

@Composable
private fun DigitConverterScreen() {
    var input by remember { mutableStateOf("۱۲۳۴۵۶7890") }
    var mode by remember { mutableStateOf("fa") }
    val result = when (mode) {
        "en" -> input.map { c -> "۰۱۲۳۴۵۶۷۸۹".indexOf(c).takeIf { it >= 0 }?.let { ('0'.code + it).toChar() } ?: "٠١٢٣٤٥٦٧٨٩".indexOf(c).takeIf { it >= 0 }?.let { ('0'.code + it).toChar() } ?: c }.joinToString("")
        "ar" -> input.map { c -> c.digitToIntOrNull()?.let { "٠١٢٣٤٥٦٧٨٩"[it] } ?: "۰۱۲۳۴۵۶۷۸۹".indexOf(c).takeIf { it >= 0 }?.let { "٠١٢٣٤٥٦٧٨٩"[it] } ?: c }.joinToString("")
        else -> input.map { c -> c.digitToIntOrNull()?.let { "۰۱۲۳۴۵۶۷۸۹"[it] } ?: "٠١٢٣٤٥٦٧٨٩".indexOf(c).takeIf { it >= 0 }?.let { "۰۱۲۳۴۵۶۷۸۹"[it] } ?: c }.joinToString("")
    }
    ToolHeader("تبدیل ارقام", "تبدیل ارقام فارسی، عربی و انگلیسی بدون تغییر متن اطراف.")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("fa" to "فارسی", "en" to "English", "ar" to "عربی").forEach { (id, label) ->
            FilterChip(selected = mode == id, onClick = { mode = id }, label = { Text(label) }, modifier = Modifier.weight(1f))
        }
    }
    CopyableTextResult("خروجی", result)
}

@Composable
private fun LineTransformScreen(mode: String) {
    var input by remember { mutableStateOf("orange\napple\norange\nbanana") }
    val lines = input.lines().filter { it.isNotBlank() }
    val result = if (mode == "sort") lines.sortedWith(String.CASE_INSENSITIVE_ORDER).joinToString("\n") else lines.distinct().joinToString("\n")
    ToolHeader(if (mode == "sort") "مرتب‌سازی خطوط" else "حذف خطوط تکراری")
    OutlinedTextField(input, { input = it }, label = { Text("هر مورد در یک خط") }, modifier = Modifier.fillMaxWidth(), minLines = 6)
    CopyableTextResult("نتیجه", result)
}

@Composable
private fun CaseConverterScreen() {
    var input by remember { mutableStateOf("App Tools Box sample text") }
    var mode by remember { mutableStateOf("lower") }
    val words = input.trim().split(Regex("[^A-Za-z0-9]+"))
    val result = when (mode) {
        "upper" -> input.uppercase()
        "title" -> input.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        "camel" -> words.mapIndexed { index, word -> if (index == 0) word.lowercase() else word.lowercase().replaceFirstChar(Char::uppercase) }.joinToString("")
        "snake" -> words.joinToString("_") { it.lowercase() }
        "kebab" -> words.joinToString("-") { it.lowercase() }
        else -> input.lowercase()
    }
    ToolHeader("Case Converter")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
    listOf(listOf("lower" to "lower", "upper" to "UPPER", "title" to "Title"), listOf("camel" to "camelCase", "snake" to "snake_case", "kebab" to "kebab-case")).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { (id, label) -> FilterChip(selected = mode == id, onClick = { mode = id }, label = { Text(label) }, modifier = Modifier.weight(1f)) }
        }
    }
    CopyableTextResult("نتیجه", result)
}

@Composable
private fun JsonFormatterScreen() {
    var input by remember { mutableStateOf("{\"name\":\"App-ToolsBox\",\"version\":2}") }
    var compact by remember { mutableStateOf(false) }
    val result = remember(input, compact) {
        runCatching {
            val trimmed = input.trim()
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).let { if (compact) it.toString() else it.toString(2) }
                trimmed.startsWith("[") -> JSONArray(trimmed).let { if (compact) it.toString() else it.toString(2) }
                else -> error("Invalid JSON")
            }
        }.getOrElse { "JSON نامعتبر: ${it.message ?: "خطا"}" }
    }
    ToolHeader("JSON Formatter", "فرمت و فشرده‌سازی JSON روی دستگاه.")
    OutlinedTextField(input, { input = it }, label = { Text("JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 6)
    FilterChip(selected = compact, onClick = { compact = !compact }, label = { Text(if (compact) "حالت فشرده" else "حالت خوانا") })
    CopyableTextResult("نتیجه", result)
}

@Composable
private fun UrlCodecScreen() {
    var input by remember { mutableStateOf("https://example.com/?q=جعبه ابزار") }
    var decode by remember { mutableStateOf(false) }
    val result = runCatching {
        if (decode) URLDecoder.decode(input, StandardCharsets.UTF_8.name()) else URLEncoder.encode(input, StandardCharsets.UTF_8.name())
    }.getOrElse { "خطا: ${it.message}" }
    ToolHeader("URL Encode / Decode")
    OutlinedTextField(input, { input = it }, label = { Text("ورودی") }, modifier = Modifier.fillMaxWidth())
    FilterChip(selected = decode, onClick = { decode = !decode }, label = { Text(if (decode) "Decode" else "Encode") })
    CopyableTextResult("نتیجه", result)
}

@Composable
private fun HtmlCodecScreen() {
    var input by remember { mutableStateOf("<b>App & Tools</b>") }
    var decode by remember { mutableStateOf(false) }
    val result = if (decode) {
        input.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&")
    } else {
        input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
    }
    ToolHeader("HTML Encode / Decode")
    OutlinedTextField(input, { input = it }, label = { Text("ورودی") }, modifier = Modifier.fillMaxWidth())
    FilterChip(selected = decode, onClick = { decode = !decode }, label = { Text(if (decode) "Decode" else "Encode") })
    CopyableTextResult("نتیجه", result)
}

@Composable
private fun UuidScreen() {
    var value by remember { mutableStateOf(UUID.randomUUID().toString()) }
    ToolHeader("UUID Generator", "تولید UUID نسخه 4 به‌صورت محلی.")
    CopyableTextResult("UUID", value)
    Button(onClick = { value = UUID.randomUUID().toString() }, modifier = Modifier.fillMaxWidth()) { Text("تولید UUID جدید") }
}

@Composable
private fun HashSuiteScreen() {
    var input by remember { mutableStateOf("App-ToolsBox") }
    var algorithm by remember { mutableStateOf("SHA-256") }
    val result = runCatching {
        MessageDigest.getInstance(algorithm).digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }.getOrElse { "نامعتبر" }
    ToolHeader("Hash Suite", "MD5، SHA-1، SHA-256 و SHA-512")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("MD5", "SHA-1", "SHA-256", "SHA-512").forEach { name ->
            FilterChip(selected = algorithm == name, onClick = { algorithm = name }, label = { Text(name) }, modifier = Modifier.weight(1f))
        }
    }
    CopyableTextResult(algorithm, result)
}

@Composable
private fun TextCompareScreen() {
    var first by remember { mutableStateOf("hello world") }
    var second by remember { mutableStateOf("hello toolbox") }
    val a = first.lines()
    val b = second.lines()
    val maxLines = maxOf(a.size, b.size)
    val changed = (0 until maxLines).count { a.getOrNull(it) != b.getOrNull(it) }
    val commonPrefix = first.commonPrefixWith(second).length
    ToolHeader("مقایسه متن", "مقایسه سبک دو متن بدون ارسال داده به سرور.")
    OutlinedTextField(first, { first = it }, label = { Text("متن اول") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
    OutlinedTextField(second, { second = it }, label = { Text("متن دوم") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
    ResultCard("نتیجه", if (first == second) "کاملاً یکسان" else "متفاوت", "$changed خط متفاوت | پیشوند مشترک: $commonPrefix کاراکتر")
}
