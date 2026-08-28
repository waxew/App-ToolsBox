package com.asteam.toolbox.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Persian/Jalali date tools introduced in v1.6.0. */
@Composable
fun PersianDateToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (toolId) {
            "jalali_today" -> JalaliTodayScreen()
            "date_converter_fa" -> PersianDateConverterScreen()
            "jalali_diff" -> JalaliDiffScreen()
            "weekday_finder" -> WeekdayFinderScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun JalaliTodayScreen() {
    val today = LocalDate.now()
    val j = gregorianToJalali(today.year, today.monthValue, today.dayOfMonth)
    val monthName = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")[j.month - 1]
    val weekday = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("fa"))
    ToolHeader("امروز شمسی", "تبدیل محلی و بدون اینترنت")
    ResultCard("تاریخ", "${j.day} $monthName ${j.year}", weekday)
    ResultCard("میلادی", today.toString())
}

@Composable
private fun PersianDateConverterScreen() {
    var mode by remember { mutableStateOf("g2j") }
    var input by remember { mutableStateOf(LocalDate.now().toString()) }
    var result by remember { mutableStateOf("") }

    ToolHeader("تبدیل تاریخ شمسی/میلادی", "میلادی: YYYY-MM-DD | شمسی: YYYY-MM-DD")
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { mode = "g2j"; input = LocalDate.now().toString() }, modifier = Modifier.weight(1f)) { Text("میلادی ← شمسی") }
        Button(onClick = { mode = "j2g"; input = "1405-06-07" }, modifier = Modifier.weight(1f)) { Text("شمسی ← میلادی") }
    }
    OutlinedTextField(input, { input = it }, label = { Text("تاریخ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Button(
        onClick = {
            result = runCatching {
                val p = input.trim().split("-").map(String::toInt)
                require(p.size == 3)
                if (mode == "g2j") {
                    val j = gregorianToJalali(p[0], p[1], p[2])
                    "%04d-%02d-%02d".format(j.year, j.month, j.day)
                } else {
                    val g = jalaliToGregorian(p[0], p[1], p[2])
                    "%04d-%02d-%02d".format(g.year, g.month, g.day)
                }
            }.getOrElse { "تاریخ نامعتبر" }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("تبدیل") }
    ResultCard("نتیجه", result.ifBlank { "—" })
}

@Composable
private fun JalaliDiffScreen() {
    var first by remember { mutableStateOf("1405-01-01") }
    var second by remember { mutableStateOf("1405-12-29") }
    val result = remember(first, second) {
        runCatching {
            fun parseJalali(text: String): LocalDate {
                val p = text.split("-").map(String::toInt)
                require(p.size == 3)
                val g = jalaliToGregorian(p[0], p[1], p[2])
                return LocalDate.of(g.year, g.month, g.day)
            }
            kotlin.math.abs(ChronoUnit.DAYS.between(parseJalali(first), parseJalali(second)))
        }.getOrNull()
    }
    ToolHeader("اختلاف تاریخ شمسی")
    OutlinedTextField(first, { first = it }, label = { Text("تاریخ اول شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(second, { second = it }, label = { Text("تاریخ دوم شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("فاصله", result?.let { "$it روز" } ?: "تاریخ نامعتبر")
}

@Composable
private fun WeekdayFinderScreen() {
    var input by remember { mutableStateOf("1405-06-07") }
    val result = remember(input) {
        runCatching {
            val p = input.split("-").map(String::toInt)
            val g = jalaliToGregorian(p[0], p[1], p[2])
            LocalDate.of(g.year, g.month, g.day).dayOfWeek.getDisplayName(TextStyle.FULL, Locale("fa"))
        }.getOrElse { "نامعتبر" }
    }
    ToolHeader("روز هفته", "روز هفته برای تاریخ شمسی")
    OutlinedTextField(input, { input = it }, label = { Text("تاریخ شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("روز", result)
}

private data class SimpleDate(val year: Int, val month: Int, val day: Int)

/** Standard arithmetic Gregorian -> Jalali conversion. */
private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): SimpleDate {
    val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var jy: Int
    var gy2 = gy
    if (gy2 > 1600) {
        jy = 979
        gy2 -= 1600
    } else {
        jy = 0
        gy2 -= 621
    }
    val gyDay = if (gm > 2) gy2 + 1 else gy2
    var days = 365 * gy2 + (gyDay + 3) / 4 - (gyDay + 99) / 100 + (gyDay + 399) / 400 - 80 + gd + gdm[gm - 1]
    jy += 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + days / 31
        jd = 1 + days % 31
    } else {
        jm = 7 + (days - 186) / 30
        jd = 1 + (days - 186) % 30
    }
    return SimpleDate(jy, jm, jd)
}

/** Standard arithmetic Jalali -> Gregorian conversion. */
private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): SimpleDate {
    var jy2 = jy
    var gy: Int
    if (jy2 > 979) {
        gy = 1600
        jy2 -= 979
    } else {
        gy = 621
    }
    var days = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4 + 78 + jd + if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
    gy += 400 * (days / 146097)
    days %= 146097
    if (days > 36524) {
        gy += 100 * (--days / 36524)
        days %= 36524
        if (days >= 365) days++
    }
    gy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        gy += (days - 1) / 365
        days = (days - 1) % 365
    }
    var gd = days + 1
    val leap = gy % 4 == 0 && (gy % 100 != 0 || gy % 400 == 0)
    val monthDays = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 0
    while (gm < 12 && gd > monthDays[gm]) {
        gd -= monthDays[gm]
        gm++
    }
    return SimpleDate(gy, gm + 1, gd)
}
