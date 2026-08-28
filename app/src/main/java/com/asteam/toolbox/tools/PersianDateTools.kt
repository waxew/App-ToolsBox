package com.asteam.toolbox.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Persian/Jalali date tools. All arithmetic runs locally without a calendar API. */
@Composable
fun PersianDateToolScreen(toolId: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (toolId) {
            "jalali_today" -> JalaliTodayScreen()
            "date_converter_fa" -> PersianDateConverterScreen()
            "jalali_diff" -> JalaliDiffScreen()
            "weekday_finder" -> WeekdayFinderScreen()
            "jalali_calendar" -> JalaliMonthScreen()
            "persian_occasions" -> PersianOccasionsScreen()
            "holiday_checker" -> HolidayCheckerScreen()
            else -> ToolHeader(title)
        }
    }
}

private val jalaliMonthNames = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
private val fixedSolarOccasions = mapOf(
    Pair(1, 1) to "نوروز",
    Pair(1, 2) to "تعطیلات نوروز",
    Pair(1, 3) to "تعطیلات نوروز",
    Pair(1, 4) to "تعطیلات نوروز",
    Pair(1, 12) to "روز جمهوری اسلامی",
    Pair(1, 13) to "روز طبیعت",
    Pair(3, 14) to "رحلت امام خمینی",
    Pair(3, 15) to "قیام ۱۵ خرداد",
    Pair(11, 22) to "پیروزی انقلاب اسلامی",
    Pair(12, 29) to "روز ملی شدن صنعت نفت",
)

@Composable
private fun JalaliTodayScreen() {
    val today = LocalDate.now()
    val j = gregorianToJalali(today.year, today.monthValue, today.dayOfMonth)
    ToolHeader("امروز شمسی", "تبدیل محلی و بدون اینترنت")
    ResultCard("تاریخ", "${j.day} ${jalaliMonthNames[j.month - 1]} ${j.year}", today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("fa")))
    ResultCard("میلادی", today.toString())
}

@Composable
private fun PersianDateConverterScreen() {
    var mode by remember { mutableStateOf("g2j") }
    var input by remember { mutableStateOf(LocalDate.now().toString()) }
    var result by remember { mutableStateOf("") }
    ToolHeader("تبدیل تاریخ شمسی/میلادی", "میلادی: YYYY-MM-DD | شمسی: YYYY-MM-DD")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { mode = "g2j"; input = LocalDate.now().toString() }, modifier = Modifier.weight(1f)) { Text("میلادی ← شمسی") }
        Button(onClick = { mode = "j2g"; input = "1405-06-07" }, modifier = Modifier.weight(1f)) { Text("شمسی ← میلادی") }
    }
    OutlinedTextField(input, { input = it }, label = { Text("تاریخ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Button(onClick = {
        result = runCatching {
            val p = input.trim().split("-").map(String::toInt)
            require(p.size == 3)
            if (mode == "g2j") {
                val j = gregorianToJalali(p[0], p[1], p[2]); "%04d-%02d-%02d".format(j.year, j.month, j.day)
            } else {
                val g = jalaliToGregorian(p[0], p[1], p[2]); "%04d-%02d-%02d".format(g.year, g.month, g.day)
            }
        }.getOrElse { "تاریخ نامعتبر" }
    }, modifier = Modifier.fillMaxWidth()) { Text("تبدیل") }
    ResultCard("نتیجه", result.ifBlank { "—" })
}

@Composable
private fun JalaliDiffScreen() {
    var first by remember { mutableStateOf("1405-01-01") }
    var second by remember { mutableStateOf("1405-12-29") }
    val result = remember(first, second) {
        runCatching { kotlin.math.abs(ChronoUnit.DAYS.between(parseJalali(first), parseJalali(second))) }.getOrNull()
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
        runCatching { parseJalali(input).dayOfWeek.getDisplayName(TextStyle.FULL, Locale("fa")) }.getOrElse { "نامعتبر" }
    }
    ToolHeader("روز هفته", "روز هفته برای تاریخ شمسی")
    OutlinedTextField(input, { input = it }, label = { Text("تاریخ شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("روز", result)
}

@Composable
private fun JalaliMonthScreen() {
    val today = LocalDate.now()
    val current = gregorianToJalali(today.year, today.monthValue, today.dayOfMonth)
    var year by remember { mutableStateOf(current.year) }
    var month by remember { mutableStateOf(current.month) }
    val days = jalaliMonthLength(year, month)
    val firstGregorian = jalaliToGregorian(year, month, 1).let { LocalDate.of(it.year, it.month, it.day) }
    val saturdayOffset = (firstGregorian.dayOfWeek.value + 1) % 7
    val cells = (List(saturdayOffset) { "" } + (1..days).map(Int::toString)).toMutableList()
    while (cells.size % 7 != 0) cells.add("")

    ToolHeader("تقویم شمسی ماهانه", "چیدمان هفته از شنبه تا جمعه")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { if (month == 1) { month = 12; year-- } else month-- }, modifier = Modifier.weight(1f)) { Text("ماه قبل") }
        Button(onClick = { if (month == 12) { month = 1; year++ } else month++ }, modifier = Modifier.weight(1f)) { Text("ماه بعد") }
    }
    ResultCard("ماه", "${jalaliMonthNames[month - 1]} $year")
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { Text(it, modifier = Modifier.weight(1f)) }
    }
    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day -> Text(day, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun PersianOccasionsScreen() {
    ToolHeader("مناسبت‌های شمسی", "فهرست ثابت مناسبت‌های خورشیدی؛ مناسبت‌های قمری به‌دلیل جابه‌جایی سالانه در این فهرست ثابت درج نشده‌اند.")
    fixedSolarOccasions.entries.sortedWith(compareBy({ it.key.first }, { it.key.second })).forEach { (date, title) ->
        ResultCard("${date.second} ${jalaliMonthNames[date.first - 1]}", title)
    }
}

@Composable
private fun HolidayCheckerScreen() {
    var input by remember { mutableStateOf("1405-01-01") }
    val result = remember(input) {
        runCatching {
            val p = input.split("-").map(String::toInt)
            require(p.size == 3)
            val date = parseJalali(input)
            val occasion = fixedSolarOccasions[Pair(p[1], p[2])]
            when {
                occasion != null -> "تعطیل/مناسبت: $occasion"
                date.dayOfWeek == DayOfWeek.FRIDAY -> "جمعه — تعطیل هفتگی"
                else -> "در فهرست ثابت تعطیلات شمسی نیست"
            }
        }.getOrElse { "تاریخ نامعتبر" }
    }
    ToolHeader("بررسی تعطیلی", "تعطیلات ثابت خورشیدی و جمعه بررسی می‌شوند؛ تعطیلات قمری سالانه نیازمند داده رسمی همان سال هستند.")
    OutlinedTextField(input, { input = it }, label = { Text("تاریخ شمسی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("نتیجه", result)
}

private fun parseJalali(text: String): LocalDate {
    val p = text.split("-").map(String::toInt)
    require(p.size == 3)
    val g = jalaliToGregorian(p[0], p[1], p[2])
    return LocalDate.of(g.year, g.month, g.day)
}

private fun jalaliMonthLength(year: Int, month: Int): Int = when {
    month in 1..6 -> 31
    month in 7..11 -> 30
    month == 12 -> {
        val g1 = jalaliToGregorian(year, 12, 1).let { LocalDate.of(it.year, it.month, it.day) }
        val g2 = jalaliToGregorian(year + 1, 1, 1).let { LocalDate.of(it.year, it.month, it.day) }
        ChronoUnit.DAYS.between(g1, g2).toInt()
    }
    else -> 0
}

private data class SimpleDate(val year: Int, val month: Int, val day: Int)

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): SimpleDate {
    val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var jy: Int; var gy2 = gy
    if (gy2 > 1600) { jy = 979; gy2 -= 1600 } else { jy = 0; gy2 -= 621 }
    val gyDay = if (gm > 2) gy2 + 1 else gy2
    var days = 365 * gy2 + (gyDay + 3) / 4 - (gyDay + 99) / 100 + (gyDay + 399) / 400 - 80 + gd + gdm[gm - 1]
    jy += 33 * (days / 12053); days %= 12053; jy += 4 * (days / 1461); days %= 1461
    if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365 }
    val jm: Int; val jd: Int
    if (days < 186) { jm = 1 + days / 31; jd = 1 + days % 31 } else { jm = 7 + (days - 186) / 30; jd = 1 + (days - 186) % 30 }
    return SimpleDate(jy, jm, jd)
}

private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): SimpleDate {
    require(jm in 1..12 && jd in 1..31)
    var jy2 = jy; var gy: Int
    if (jy2 > 979) { gy = 1600; jy2 -= 979 } else gy = 621
    var days = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4 + 78 + jd + if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
    gy += 400 * (days / 146097); days %= 146097
    if (days > 36524) { gy += 100 * (--days / 36524); days %= 36524; if (days >= 365) days++ }
    gy += 4 * (days / 1461); days %= 1461
    if (days > 365) { gy += (days - 1) / 365; days = (days - 1) % 365 }
    var gd = days + 1
    val leap = gy % 4 == 0 && (gy % 100 != 0 || gy % 400 == 0)
    val monthDays = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 0
    while (gm < 12 && gd > monthDays[gm]) { gd -= monthDays[gm]; gm++ }
    return SimpleDate(gy, gm + 1, gd)
}
