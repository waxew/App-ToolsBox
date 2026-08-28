package com.asteam.toolbox.tools

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
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

private val moneyFormat = DecimalFormat("#,##0.##")
private val decimalFormat = DecimalFormat("0.####")

/** Advanced calculators added in v1.3.0. */
@Composable
fun AdvancedCalculationToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "compound_interest" -> CompoundInterestScreen()
            "percent_change" -> PercentChangeScreen()
            "reverse_tax" -> ReverseTaxScreen()
            "multi_discount" -> MultiDiscountScreen()
            "payroll" -> PayrollScreen()
            "overtime" -> OvertimeScreen()
            "exact_age_plus" -> ExactAgePlusScreen()
            "business_days" -> BusinessDaysScreen()
            "shape_area" -> ShapeAreaScreen()
            "solid_volume" -> SolidVolumeScreen()
            "pythagorean" -> PythagoreanScreen()
            "quadratic" -> QuadraticEquationScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun CompoundInterestScreen() {
    var principal by remember { mutableStateOf("10000000") }
    var annualRate by remember { mutableStateOf("20") }
    var years by remember { mutableStateOf("2") }
    var compounds by remember { mutableStateOf("12") }

    val p = principal.toDoubleOrNull() ?: 0.0
    val r = (annualRate.toDoubleOrNull() ?: 0.0) / 100.0
    val y = years.toDoubleOrNull() ?: 0.0
    val n = (compounds.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
    val amount = if (p >= 0 && y >= 0) p * (1.0 + r / n).pow(n * y) else 0.0

    ToolHeader("سود مرکب", "اصل سرمایه، نرخ سالانه، مدت و تعداد دفعات ترکیب را وارد کنید.")
    NumberField("اصل سرمایه", principal, { principal = it })
    NumberField("نرخ سالانه (%)", annualRate, { annualRate = it })
    NumberField("مدت (سال)", years, { years = it })
    NumberField("تعداد ترکیب در سال", compounds, { compounds = it })
    ResultCard("ارزش نهایی", moneyFormat.format(amount), "سود: ${moneyFormat.format(amount - p)}")
}

@Composable
private fun PercentChangeScreen() {
    var oldValue by remember { mutableStateOf("100") }
    var newValue by remember { mutableStateOf("125") }
    val old = oldValue.toDoubleOrNull()
    val new = newValue.toDoubleOrNull()
    val result = if (old != null && new != null && old != 0.0) ((new - old) / kotlin.math.abs(old)) * 100.0 else null

    ToolHeader("تغییر درصد", "میزان افزایش یا کاهش بین مقدار قبلی و جدید.")
    NumberField("مقدار قبلی", oldValue, { oldValue = it })
    NumberField("مقدار جدید", newValue, { newValue = it })
    ResultCard("تغییر", result?.let { "${decimalFormat.format(it)}%" } ?: "نامعتبر", result?.let { if (it >= 0) "افزایش" else "کاهش" })
}

@Composable
private fun ReverseTaxScreen() {
    var finalPrice by remember { mutableStateOf("1100000") }
    var taxRate by remember { mutableStateOf("10") }
    val total = finalPrice.toDoubleOrNull() ?: 0.0
    val rate = (taxRate.toDoubleOrNull() ?: 0.0) / 100.0
    val base = if (rate > -1.0) total / (1.0 + rate) else 0.0

    ToolHeader("مالیات معکوس", "از مبلغ نهایی، مبلغ قبل از مالیات و سهم مالیات را استخراج می‌کند.")
    NumberField("مبلغ نهایی", finalPrice, { finalPrice = it })
    NumberField("نرخ مالیات (%)", taxRate, { taxRate = it })
    ResultCard("قبل از مالیات", moneyFormat.format(base), "مالیات: ${moneyFormat.format(total - base)}")
}

@Composable
private fun MultiDiscountScreen() {
    var price by remember { mutableStateOf("1000000") }
    var first by remember { mutableStateOf("10") }
    var second by remember { mutableStateOf("5") }
    var third by remember { mutableStateOf("0") }

    val original = price.toDoubleOrNull() ?: 0.0
    val discounts = listOf(first, second, third).map { (it.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0) }
    val final = discounts.fold(original) { acc, discount -> acc * (1.0 - discount / 100.0) }
    val effective = if (original > 0) (1.0 - final / original) * 100.0 else 0.0

    ToolHeader("تخفیف چندمرحله‌ای", "تخفیف‌های پیاپی با هم جمع ساده نمی‌شوند؛ این ابزار اثر واقعی را حساب می‌کند.")
    NumberField("قیمت اولیه", price, { price = it })
    NumberField("تخفیف اول (%)", first, { first = it })
    NumberField("تخفیف دوم (%)", second, { second = it })
    NumberField("تخفیف سوم (%)", third, { third = it })
    ResultCard("قیمت نهایی", moneyFormat.format(final), "تخفیف مؤثر: ${decimalFormat.format(effective)}% | صرفه‌جویی: ${moneyFormat.format(original - final)}")
}

@Composable
private fun PayrollScreen() {
    var baseSalary by remember { mutableStateOf("15000000") }
    var benefits by remember { mutableStateOf("0") }
    var deductions by remember { mutableStateOf("0") }
    var taxRate by remember { mutableStateOf("0") }

    val base = baseSalary.toDoubleOrNull() ?: 0.0
    val extra = benefits.toDoubleOrNull() ?: 0.0
    val deduction = deductions.toDoubleOrNull() ?: 0.0
    val gross = base + extra
    val tax = gross * ((taxRate.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0) / 100.0)
    val net = gross - deduction - tax

    ToolHeader("حقوق خالص", "محاسبه عمومی حقوق؛ قوانین بیمه و مالیات هر کشور/سال باید جداگانه اعمال شوند.")
    NumberField("حقوق پایه", baseSalary, { baseSalary = it })
    NumberField("مزایا", benefits, { benefits = it })
    NumberField("کسورات ثابت", deductions, { deductions = it })
    NumberField("نرخ مالیات (%)", taxRate, { taxRate = it })
    ResultCard("حقوق ناخالص", moneyFormat.format(gross), "مالیات: ${moneyFormat.format(tax)}")
    ResultCard("خالص دریافتی", moneyFormat.format(net))
}

@Composable
private fun OvertimeScreen() {
    var monthlySalary by remember { mutableStateOf("15000000") }
    var standardHours by remember { mutableStateOf("176") }
    var overtimeHours by remember { mutableStateOf("10") }
    var multiplier by remember { mutableStateOf("1.4") }

    val salary = monthlySalary.toDoubleOrNull() ?: 0.0
    val hours = (standardHours.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
    val overtime = (overtimeHours.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val factor = (multiplier.toDoubleOrNull() ?: 1.0).coerceAtLeast(0.0)
    val hourly = salary / hours
    val pay = hourly * overtime * factor

    ToolHeader("اضافه‌کاری", "ضریب اضافه‌کاری قابل تنظیم است تا با قرارداد یا مقررات محل کار هماهنگ شود.")
    NumberField("حقوق ماهانه", monthlySalary, { monthlySalary = it })
    NumberField("ساعت استاندارد ماه", standardHours, { standardHours = it })
    NumberField("ساعت اضافه‌کاری", overtimeHours, { overtimeHours = it })
    NumberField("ضریب", multiplier, { multiplier = it })
    ResultCard("نرخ ساعتی", moneyFormat.format(hourly), "اضافه‌کاری: ${moneyFormat.format(pay)}")
}

@Composable
private fun ExactAgePlusScreen() {
    var birth by remember { mutableStateOf("2000-01-01") }
    var target by remember { mutableStateOf(LocalDate.now().toString()) }
    val data = remember(birth, target) {
        runCatching {
            val b = LocalDate.parse(birth)
            val t = LocalDate.parse(target)
            require(!t.isBefore(b))
            val period = Period.between(b, t)
            val days = ChronoUnit.DAYS.between(b, t)
            Triple(period, days, ChronoUnit.WEEKS.between(b, t))
        }.getOrNull()
    }

    ToolHeader("سن دقیق پیشرفته", "فرمت ورودی YYYY-MM-DD است.")
    OutlinedTextField(birth, { birth = it }, label = { Text("تاریخ تولد") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(target, { target = it }, label = { Text("تاریخ مقصد") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    if (data == null) {
        ResultCard("نتیجه", "تاریخ نامعتبر")
    } else {
        val (period, days, weeks) = data
        ResultCard("سن", "${period.years} سال، ${period.months} ماه، ${period.days} روز", "$days روز | $weeks هفته")
    }
}

@Composable
private fun BusinessDaysScreen() {
    var from by remember { mutableStateOf(LocalDate.now().toString()) }
    var to by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    val result = remember(from, to) {
        runCatching {
            var start = LocalDate.parse(from)
            var end = LocalDate.parse(to)
            if (start.isAfter(end)) start = end.also { end = start }
            var current = start
            var count = 0L
            while (!current.isAfter(end)) {
                if (current.dayOfWeek != DayOfWeek.FRIDAY) count++
                current = current.plusDays(1)
            }
            count
        }.getOrNull()
    }

    ToolHeader("روزهای کاری", "در این محاسبه جمعه تعطیل هفتگی در نظر گرفته شده؛ تعطیلات رسمی متغیر جداگانه محاسبه نمی‌شوند.")
    OutlinedTextField(from, { from = it }, label = { Text("از تاریخ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(to, { to = it }, label = { Text("تا تاریخ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    ResultCard("روز کاری", result?.toString() ?: "تاریخ نامعتبر")
}

@Composable
private fun ShapeAreaScreen() {
    var shape by remember { mutableStateOf("rectangle") }
    var a by remember { mutableStateOf("10") }
    var b by remember { mutableStateOf("5") }

    ToolHeader("مساحت اشکال", "مستطیل، مثلث و دایره")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("rectangle" to "مستطیل", "triangle" to "مثلث", "circle" to "دایره").forEach { (id, label) ->
            FilterChip(selected = shape == id, onClick = { shape = id }, label = { Text(label) }, modifier = Modifier.weight(1f))
        }
    }
    NumberField(if (shape == "circle") "شعاع" else "مقدار A", a, { a = it })
    if (shape != "circle") NumberField(if (shape == "triangle") "ارتفاع" else "مقدار B", b, { b = it })
    val av = a.toDoubleOrNull() ?: 0.0
    val bv = b.toDoubleOrNull() ?: 0.0
    val area = when (shape) {
        "triangle" -> av * bv / 2.0
        "circle" -> PI * av * av
        else -> av * bv
    }
    ResultCard("مساحت", decimalFormat.format(area))
}

@Composable
private fun SolidVolumeScreen() {
    var solid by remember { mutableStateOf("box") }
    var a by remember { mutableStateOf("10") }
    var b by remember { mutableStateOf("5") }
    var c by remember { mutableStateOf("2") }

    ToolHeader("حجم اشکال", "مکعب‌مستطیل، استوانه و کره")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("box" to "جعبه", "cylinder" to "استوانه", "sphere" to "کره").forEach { (id, label) ->
            FilterChip(selected = solid == id, onClick = { solid = id }, label = { Text(label) }, modifier = Modifier.weight(1f))
        }
    }
    NumberField(if (solid == "box") "طول" else "شعاع", a, { a = it })
    if (solid == "box") NumberField("عرض", b, { b = it })
    if (solid != "sphere") NumberField(if (solid == "box") "ارتفاع" else "ارتفاع", c, { c = it })
    val av = a.toDoubleOrNull() ?: 0.0
    val bv = b.toDoubleOrNull() ?: 0.0
    val cv = c.toDoubleOrNull() ?: 0.0
    val volume = when (solid) {
        "cylinder" -> PI * av * av * cv
        "sphere" -> 4.0 / 3.0 * PI * av.pow(3)
        else -> av * bv * cv
    }
    ResultCard("حجم", decimalFormat.format(volume))
}

@Composable
private fun PythagoreanScreen() {
    var a by remember { mutableStateOf("3") }
    var b by remember { mutableStateOf("4") }
    val av = a.toDoubleOrNull() ?: 0.0
    val bv = b.toDoubleOrNull() ?: 0.0
    val c = sqrt(av * av + bv * bv)

    ToolHeader("فیثاغورس", "محاسبه وتر مثلث قائم‌الزاویه")
    NumberField("ضلع A", a, { a = it })
    NumberField("ضلع B", b, { b = it })
    ResultCard("وتر", decimalFormat.format(c))
}

@Composable
private fun QuadraticEquationScreen() {
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("-3") }
    var c by remember { mutableStateOf("2") }
    val av = a.toDoubleOrNull()
    val bv = b.toDoubleOrNull()
    val cv = c.toDoubleOrNull()

    ToolHeader("معادله درجه‌دو", "حل ax² + bx + c = 0")
    NumberField("a", a, { a = it })
    NumberField("b", b, { b = it })
    NumberField("c", c, { c = it })

    val answer = when {
        av == null || bv == null || cv == null -> "ضرایب نامعتبر"
        av == 0.0 -> if (bv == 0.0) "معادله معتبر نیست" else "x = ${decimalFormat.format(-cv / bv)}"
        else -> {
            val delta = bv * bv - 4.0 * av * cv
            when {
                delta > 0 -> {
                    val root = sqrt(delta)
                    "x₁ = ${decimalFormat.format((-bv + root) / (2 * av))} | x₂ = ${decimalFormat.format((-bv - root) / (2 * av))}"
                }
                delta == 0.0 -> "x = ${decimalFormat.format(-bv / (2 * av))}"
                else -> "ریشه حقیقی ندارد (Δ = ${decimalFormat.format(delta)})"
            }
        }
    }
    ResultCard("پاسخ", answer)
}
