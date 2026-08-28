package com.asteam.toolbox.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.asteam.toolbox.util.ExpressionEvaluator
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToLong

private val numberFormat = DecimalFormat("#,##0.########")
private fun String.num(): Double = replace(",", "").toDoubleOrNull() ?: 0.0
private fun Double.pretty(): String = if (isFinite()) numberFormat.format(this) else "نامعتبر"

@Composable
fun CalculationToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (toolId) {
            "calculator" -> ScientificCalculator()
            "percentage" -> PercentageCalculator()
            "discount" -> DiscountCalculator()
            "tax" -> TaxCalculator()
            "profit" -> ProfitCalculator()
            "loan" -> LoanCalculator()
            "bmi" -> BmiCalculator()
            "bmr" -> BmrCalculator()
            "split" -> SplitBillCalculator()
            "ratio" -> RatioCalculator()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun ScientificCalculator() {
    var expression by remember { mutableStateOf("2 + 2") }
    var result by remember { mutableStateOf("4") }
    var error by remember { mutableStateOf<String?>(null) }
    ToolHeader("ماشین‌حساب علمی", "عملگرها: + − × ÷ ^، پرانتز، sqrt، sin، cos، tan، log، ln، abs، pi و e")
    OutlinedTextField(expression, { expression = it }, label = { Text("عبارت") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    Button(
        onClick = {
            runCatching { ExpressionEvaluator.evaluate(expression) }
                .onSuccess { result = it.pretty(); error = null }
                .onFailure { error = it.message ?: "عبارت نامعتبر است" }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("محاسبه") }
    ResultCard("نتیجه", result, error)
}

@Composable
private fun PercentageCalculator() {
    var base by remember { mutableStateOf("100000") }
    var percent by remember { mutableStateOf("20") }
    ToolHeader("محاسبه درصد", "درصد دلخواه از یک عدد")
    NumberField("عدد", base, { base = it })
    NumberField("درصد", percent, { percent = it })
    ResultCard("نتیجه", (base.num() * percent.num() / 100.0).pretty(), "${percent.num().pretty()}٪ از ${base.num().pretty()}")
}

@Composable
private fun DiscountCalculator() {
    var price by remember { mutableStateOf("1200000") }
    var percent by remember { mutableStateOf("15") }
    val discount = price.num() * percent.num() / 100.0
    ToolHeader("تخفیف", "مبلغ تخفیف و قیمت نهایی")
    NumberField("قیمت اولیه", price, { price = it })
    NumberField("درصد تخفیف", percent, { percent = it })
    ResultCard("قیمت نهایی", (price.num() - discount).pretty(), "مقدار تخفیف: ${discount.pretty()}")
}

@Composable
private fun TaxCalculator() {
    var amount by remember { mutableStateOf("1000000") }
    var percent by remember { mutableStateOf("10") }
    val tax = amount.num() * percent.num() / 100.0
    ToolHeader("مالیات", "افزودن درصد مالیات به مبلغ پایه")
    NumberField("مبلغ پایه", amount, { amount = it })
    NumberField("درصد مالیات", percent, { percent = it })
    ResultCard("مبلغ با مالیات", (amount.num() + tax).pretty(), "مالیات: ${tax.pretty()}")
}

@Composable
private fun ProfitCalculator() {
    var buy by remember { mutableStateOf("800000") }
    var sell by remember { mutableStateOf("1000000") }
    val profit = sell.num() - buy.num()
    val rate = if (buy.num() == 0.0) 0.0 else profit / buy.num() * 100.0
    ToolHeader("سود", "سود یا زیان مبلغی و درصدی")
    NumberField("قیمت خرید", buy, { buy = it })
    NumberField("قیمت فروش", sell, { sell = it })
    ResultCard("سود / زیان", profit.pretty(), "درصد: ${rate.pretty()}٪")
}

@Composable
private fun LoanCalculator() {
    var principal by remember { mutableStateOf("100000000") }
    var annualRate by remember { mutableStateOf("18") }
    var months by remember { mutableStateOf("24") }
    val p = principal.num()
    val n = months.num().roundToLong().coerceAtLeast(1).toDouble()
    val monthlyRate = annualRate.num() / 1200.0
    val monthly = when {
        p <= 0.0 -> 0.0
        monthlyRate == 0.0 -> p / n
        else -> p * monthlyRate * (1 + monthlyRate).pow(n) / ((1 + monthlyRate).pow(n) - 1)
    }
    val total = monthly * n
    ToolHeader("وام و قسط", "محاسبه اقساط مساوی بر مبنای نرخ سالانه")
    NumberField("اصل وام", principal, { principal = it })
    NumberField("نرخ سالانه (درصد)", annualRate, { annualRate = it })
    NumberField("تعداد ماه", months, { months = it })
    ResultCard("قسط ماهانه", monthly.pretty(), "کل بازپرداخت: ${total.pretty()} | سود کل: ${(total - p).pretty()}")
}

@Composable
private fun BmiCalculator() {
    var weight by remember { mutableStateOf("70") }
    var heightCm by remember { mutableStateOf("175") }
    val heightM = heightCm.num() / 100.0
    val bmi = if (heightM > 0) weight.num() / (heightM * heightM) else 0.0
    val label = when { bmi <= 0 -> "-"; bmi < 18.5 -> "کم‌وزن"; bmi < 25 -> "محدوده معمول"; bmi < 30 -> "اضافه‌وزن"; else -> "چاقی" }
    ToolHeader("BMI", "شاخص توده بدنی؛ صرفاً یک محاسبه عمومی و نه تشخیص پزشکی")
    NumberField("وزن (کیلوگرم)", weight, { weight = it })
    NumberField("قد (سانتی‌متر)", heightCm, { heightCm = it })
    ResultCard("BMI", bmi.pretty(), label)
}

@Composable
private fun BmrCalculator() {
    var sex by remember { mutableStateOf(0) }
    var weight by remember { mutableStateOf("70") }
    var height by remember { mutableStateOf("175") }
    var age by remember { mutableStateOf("30") }
    val bmr = if (sex == 0) 10 * weight.num() + 6.25 * height.num() - 5 * age.num() + 5 else 10 * weight.num() + 6.25 * height.num() - 5 * age.num() - 161
    ToolHeader("BMR", "برآورد متابولیسم پایه با فرمول Mifflin–St Jeor")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        listOf("مرد", "زن").forEachIndexed { index, label ->
            SegmentedButton(selected = sex == index, onClick = { sex = index }, shape = SegmentedButtonDefaults.itemShape(index, 2)) { Text(label) }
        }
    }
    NumberField("وزن (کیلوگرم)", weight, { weight = it })
    NumberField("قد (سانتی‌متر)", height, { height = it })
    NumberField("سن (سال)", age, { age = it })
    ResultCard("انرژی پایه تقریبی", "${bmr.pretty()} kcal/day", "این عدد تخمینی است.")
}

@Composable
private fun SplitBillCalculator() {
    var amount by remember { mutableStateOf("1000000") }
    var tip by remember { mutableStateOf("10") }
    var people by remember { mutableStateOf("4") }
    val total = amount.num() * (1 + tip.num() / 100.0)
    val count = people.num().coerceAtLeast(1.0)
    ToolHeader("تقسیم صورتحساب")
    NumberField("مبلغ", amount, { amount = it })
    NumberField("انعام (درصد)", tip, { tip = it })
    NumberField("تعداد افراد", people, { people = it })
    ResultCard("سهم هر نفر", (total / count).pretty(), "جمع نهایی: ${total.pretty()}")
}

@Composable
private fun RatioCalculator() {
    var a by remember { mutableStateOf("1920") }
    var b by remember { mutableStateOf("1080") }
    val ai = a.num().roundToLong(); val bi = b.num().roundToLong()
    fun gcd(x: Long, y: Long): Long {
        var m = kotlin.math.abs(x); var n = kotlin.math.abs(y)
        while (n != 0L) { val t = m % n; m = n; n = t }
        return if (m == 0L) 1L else m
    }
    val g = gcd(ai, bi)
    ToolHeader("ساده‌سازی نسبت")
    NumberField("مقدار اول", a, { a = it })
    NumberField("مقدار دوم", b, { b = it })
    ResultCard("نسبت ساده", "${ai / g} : ${bi / g}")
}
