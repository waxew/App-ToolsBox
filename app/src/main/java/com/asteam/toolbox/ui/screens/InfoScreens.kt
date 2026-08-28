package com.asteam.toolbox.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.BuildConfig
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader(
            "درباره نرم‌افزار",
            "جعبه ابزار یک مجموعه فارسی و آفلاین‌محور از ابزارهای روزمره، محاسباتی، تبدیل واحد، سنسورها و اطلاعات دستگاه است.",
        )
        Text(
            "هدف برنامه این است که چندین ابزار کوچک و کاربردی را بدون نیاز به نصب برنامه‌های متعدد در اختیار کاربر قرار دهد. ابزارهای حساس فقط هنگام استفاده، مجوز لازم را درخواست می‌کنند.",
            style = MaterialTheme.typography.bodyLarge,
        )
        ResultCard("نسخه", BuildConfig.VERSION_NAME)
        Text("توسعه: گروه توسعه فناوری و نرم افزاری as Team", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val email = "AS.Support.info@gmail.com"
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader("ارتباط با ما", "برای گزارش خطا، پیشنهاد ابزار جدید یا بازخورد درباره برنامه با تیم توسعه تماس بگیرید.")
        ResultCard("ایمیل", email)
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                runCatching { context.startActivity(intent) }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("ارسال ایمیل") }
        Text(
            "گروه توسعه فناوری و نرم افزاری as Team",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(top = 72.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader("تنظیمات")
        ResultCard("حریم خصوصی", "Offline-first", "داده‌های پروفایل، علاقه‌مندی‌ها و تنظیمات روی دستگاه ذخیره می‌شوند.")
        ResultCard("مجوزها", "در لحظه نیاز", "دوربین، میکروفون یا موقعیت فقط برای ابزار مرتبط و هنگام استفاده درخواست می‌شود.")
        ResultCard("حفظ اطلاعات در آپدیت", "فعال", "تا زمانی که برنامه حذف یا داده‌های آن پاک نشود، تنظیمات کاربر باقی می‌ماند.")
    }
}
