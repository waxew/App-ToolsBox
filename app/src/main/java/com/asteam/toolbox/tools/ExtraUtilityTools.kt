package com.asteam.toolbox.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Small offline utilities that complete the 100+ v2 catalog. */
@Composable
fun ExtraUtilityToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (toolId) {
            "unix_time" -> UnixTimeScreen()
            "roman_number" -> RomanNumberScreen()
            "gcd_lcm" -> GcdLcmScreen()
            "slugify" -> SlugifyScreen()
            "reverse_text" -> ReverseTextScreen()
            "line_numbering" -> LineNumberingScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun UnixTimeScreen() {
    var value by remember { mutableStateOf((System.currentTimeMillis() / 1000L).toString()) }
    val seconds = value.toLongOrNull()
    val formatted = seconds?.let {
        runCatching {
            Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
        }.getOrNull()
    }
    ToolHeader("Unix Time")
    NumberField("Timestamp (seconds)", value, { value = it })
    ResultCard("تاریخ محلی", formatted ?: "نامعتبر")
}

@Composable
private fun RomanNumberScreen() {
    var value by remember { mutableStateOf("2026") }
    val number = value.toIntOrNull()
    val result = if (number != null && number in 1..3999) toRoman(number) else "بازه معتبر 1 تا 3999"
    ToolHeader("اعداد رومی")
    NumberField("عدد", value, { value = it })
    ResultCard("Roman", result)
}

@Composable
private fun GcdLcmScreen() {
    var a by remember { mutableStateOf("12") }
    var b by remember { mutableStateOf("18") }
    val av = a.toLongOrNull()
    val bv = b.toLongOrNull()
    val gcd = if (av != null && bv != null) gcd(abs(av), abs(bv)) else null
    val lcm = if (gcd != null && gcd != 0L) abs(av!! / gcd * bv!!) else 0L
    ToolHeader("ب.م.م و ک.م.م")
    NumberField("عدد اول", a, { a = it })
    NumberField("عدد دوم", b, { b = it })
    ResultCard("ب.م.م", gcd?.toString() ?: "نامعتبر", "ک.م.م: ${if (gcd == null) "—" else lcm}")
}

@Composable
private fun SlugifyScreen() {
    var input by remember { mutableStateOf("App Tools Box ابزار کاربردی") }
    val result = input.trim().lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "-").trim('-')
    ToolHeader("Slug ساز")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
    ResultCard("Slug", result)
}

@Composable
private fun ReverseTextScreen() {
    var input by remember { mutableStateOf("جعبه ابزار") }
    ToolHeader("معکوس متن")
    OutlinedTextField(input, { input = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
    ResultCard("نتیجه", input.reversed())
}

@Composable
private fun LineNumberingScreen() {
    var input by remember { mutableStateOf("خط اول\nخط دوم\nخط سوم") }
    val result = input.lines().mapIndexed { index, line -> "${index + 1}. $line" }.joinToString("\n")
    ToolHeader("شماره‌گذاری خطوط")
    OutlinedTextField(input, { input = it }, label = { Text("متن چندخطی") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
    ResultCard("نتیجه", result)
}

private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

private fun toRoman(number: Int): String {
    var n = number
    val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    return buildString {
        values.indices.forEach { i ->
            while (n >= values[i]) {
                append(symbols[i])
                n -= values[i]
            }
        }
    }
}
