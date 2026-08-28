package com.asteam.toolbox.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.data.UserPreferences
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** User-controlled local backup/import using Android's Storage Access Framework. */
@Composable
fun BackupRestoreScreen(preferences: UserPreferences) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("آماده") }

    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        status = runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(preferences.exportBackupJson())
            } ?: error("Unable to open output stream")
            "فایل پشتیبان با موفقیت ذخیره شد."
        }.getOrElse { "ذخیره Backup ناموفق بود: ${it.message ?: "خطا"}" }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        status = runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Unable to read backup")
            preferences.importBackupJson(raw).getOrThrow()
            "Backup وارد شد. برای اعمال کامل ظاهر، صفحه را دوباره باز کنید."
        }.getOrElse { "ورود Backup ناموفق بود: ${it.message ?: "فایل نامعتبر"}" }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolHeader(
            "پشتیبان‌گیری و بازیابی",
            "تنظیمات، علاقه‌مندی‌ها، شمارنده، تاریخچه اسکن و شخصی‌سازی در یک فایل JSON قابل‌انتقال ذخیره می‌شوند.",
        )
        Button(
            onClick = {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                createLauncher.launch("App-ToolsBox-backup-$timestamp.json")
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("خروجی Backup") }

        Button(
            onClick = { openLauncher.launch(arrayOf("application/json", "text/plain")) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("بازیابی از Backup") }

        ResultCard("وضعیت", status)
        ResultCard(
            "حریم خصوصی",
            "فایل فقط با انتخاب خود کاربر ساخته یا خوانده می‌شود.",
            "URI تصویر پروفایل به دلیل وابستگی به دستگاه در Backup منتقل نمی‌شود.",
        )
    }
}
