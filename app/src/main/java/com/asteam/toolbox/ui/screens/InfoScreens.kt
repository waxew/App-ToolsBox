package com.asteam.toolbox.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.BuildConfig
import com.asteam.toolbox.data.UserPreferences
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
            "جعبه ابزار یک مجموعه فارسی، ماژولار و آفلاین‌محور از بیش از صد ابزار روزمره، محاسباتی، سنسوری، توسعه و شبکه است.",
        )
        Text(
            "ابزارهای حساس فقط هنگام استفاده مجوز لازم را درخواست می‌کنند. تنظیمات و داده‌های شخصی اصلی روی خود دستگاه نگه‌داری می‌شوند.",
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
            onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))) } },
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
fun SettingsScreen(preferences: UserPreferences, onChanged: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ToolHeader("تنظیمات", "شخصی‌سازی در حافظه محلی ذخیره می‌شود و بعد از آپدیت باقی می‌ماند.")

        Text("حالت نمایش", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("system" to "سیستم", "light" to "روشن", "dark" to "تیره").forEach { (id, label) ->
                FilterChip(selected = preferences.themeMode == id, onClick = { preferences.themeMode = id; onChanged() }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }

        Text("رنگ اصلی", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("blue" to "آبی", "green" to "سبز", "orange" to "نارنجی", "purple" to "بنفش").forEach { (id, label) ->
                FilterChip(selected = preferences.accentColor == id, onClick = { preferences.accentColor = id; onChanged() }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }

        Text("چیدمان صفحه اصلی", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = preferences.homeLayout == "grid", onClick = { preferences.homeLayout = "grid"; onChanged() }, label = { Text("شبکه‌ای") }, modifier = Modifier.weight(1f))
            FilterChip(selected = preferences.homeLayout == "list", onClick = { preferences.homeLayout = "list"; onChanged() }, label = { Text("فهرستی") }, modifier = Modifier.weight(1f))
        }

        Text("مرتب‌سازی", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("catalog" to "پیش‌فرض", "title" to "نام", "recent" to "اخیر").forEach { (id, label) ->
                FilterChip(selected = preferences.sortMode == id, onClick = { preferences.sortMode = id; onChanged() }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }

        Text("اندازه کارت‌ها", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("compact" to "فشرده", "normal" to "معمولی", "large" to "بزرگ").forEach { (id, label) ->
                FilterChip(selected = preferences.cardSize == id, onClick = { preferences.cardSize = id; onChanged() }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }

        ResultCard("مجموعه من", "${preferences.customCollection().size} ابزار", "با آیکون نشانک روی کارت‌ها مدیریت می‌شود.")
        ResultCard("ابزارهای مخفی", preferences.hiddenTools().size.toString(), "با آیکون چشم روی کارت ابزار مخفی می‌شود.")
        if (preferences.hiddenTools().isNotEmpty()) {
            Button(
                onClick = {
                    preferences.hiddenTools().toList().forEach { preferences.setToolHidden(it, false) }
                    onChanged()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("نمایش دوباره همه ابزارهای مخفی") }
        }

        ResultCard("حریم خصوصی", "Offline-first", "داده‌های پروفایل، علاقه‌مندی‌ها و تنظیمات روی دستگاه ذخیره می‌شوند.")
        ResultCard("مجوزها", "در لحظه نیاز", "دوربین، میکروفون، موقعیت یا اعلان فقط برای قابلیت مرتبط درخواست می‌شود.")
        ResultCard("حفظ اطلاعات در آپدیت", "فعال", "تا زمانی که برنامه حذف یا داده‌های آن پاک نشود، تنظیمات کاربر باقی می‌ماند.")
    }
}
