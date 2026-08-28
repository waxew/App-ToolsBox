package com.asteam.toolbox.tools

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.asteam.toolbox.system.ReminderReceiver
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader

/** Inexact local reminder; intentionally avoids exact-alarm special access. */
@Composable
fun LocalReminderScreen() {
    val context = LocalContext.current
    var minutes by remember { mutableStateOf("10") }
    var message by remember { mutableStateOf("یادآوری از جعبه ابزار") }
    var status by remember { mutableStateOf("آماده") }
    var notificationGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED,
        )
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationGranted = it
    }

    ToolHeader("یادآور محلی", "یادآوری روی همین دستگاه ثبت می‌شود و برای آن حساب کاربری یا اینترنت لازم نیست.")
    NumberField("چند دقیقه دیگر", minutes, { minutes = it })
    OutlinedTextField(
        value = message,
        onValueChange = { message = it },
        label = { Text("متن یادآوری") },
        modifier = Modifier.fillMaxWidth(),
    )

    if (!notificationGranted && Build.VERSION.SDK_INT >= 33) {
        Button(
            onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("اجازه اعلان") }
    }

    Button(
        onClick = {
            val delayMinutes = minutes.toLongOrNull()?.takeIf { it in 1..525_600 }
            if (delayMinutes == null) {
                status = "زمان نامعتبر است."
                return@Button
            }
            if (Build.VERSION.SDK_INT >= 33 && !notificationGranted) {
                status = "ابتدا اجازه اعلان را فعال کنید."
                return@Button
            }

            val requestCode = (System.currentTimeMillis() and 0x7fffffff).toInt()
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_MESSAGE, message.ifBlank { "یادآوری جعبه ابزار" })
                putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = SystemClock.elapsedRealtime() + delayMinutes * 60_000L
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            status = "یادآوری برای $delayMinutes دقیقه دیگر ثبت شد."
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("ثبت یادآوری") }

    ResultCard("وضعیت", status)
}
