package com.asteam.toolbox.tools

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.ui.components.NumberField
import com.asteam.toolbox.ui.components.ResultCard
import com.asteam.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.URL

/** Network diagnostics introduced in v1.5 and completed for v2.0. */
@Composable
fun NetworkToolScreen(toolId: String, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (toolId) {
            "network_state" -> NetworkStateScreen()
            "local_ip" -> LocalIpScreen()
            "public_ip" -> PublicIpScreen()
            "dns_lookup" -> DnsLookupScreen()
            "ping_host" -> PingScreen()
            "port_test" -> PortTestScreen()
            "wifi_info" -> WifiInfoScreen()
            "whois_lookup" -> WhoisScreen()
            "network_speed" -> NetworkSpeedScreen()
            "data_usage" -> DataUsageScreen()
            else -> ToolHeader(title)
        }
    }
}

@Composable
private fun NetworkStateScreen() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    val network = manager.activeNetwork
    val caps = network?.let(manager::getNetworkCapabilities)
    val type = when {
        caps == null -> "بدون اتصال"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "شبکه موبایل"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> "شبکه دیگر"
    }
    val validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    ToolHeader("وضعیت اتصال")
    ResultCard("نوع اتصال", type, if (validated) "دسترسی اینترنت تأیید شده" else "اینترنت تأیید نشده")
}

@Composable
private fun LocalIpScreen() {
    val result = remember {
        runCatching {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .filter { !it.isLoopbackAddress }
                .joinToString("\n") { address -> "${address.hostAddress} (${if (address.address.size == 4) "IPv4" else "IPv6"})" }
        }.getOrElse { "دریافت IP ناموفق بود" }
    }
    ToolHeader("IP محلی", "آدرس‌های فعال دستگاه در شبکه محلی.")
    ResultCard("IP", result.ifBlank { "آدرس فعالی پیدا نشد" })
}

@Composable
private fun PublicIpScreen() {
    var state by remember { mutableStateOf("در حال دریافت…") }
    LaunchedEffect(Unit) {
        state = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL("https://api.ipify.org").openConnection() as HttpURLConnection
                connection.connectTimeout = 4_000
                connection.readTimeout = 4_000
                connection.requestMethod = "GET"
                try {
                    connection.inputStream.bufferedReader().use { it.readText().trim() }
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { "دریافت IP عمومی ناموفق بود" }
        }
    }
    ToolHeader("IP عمومی", "این ابزار برای نمایش IP عمومی یک درخواست کوتاه HTTPS به api.ipify.org می‌فرستد.")
    ResultCard("Public IP", state)
}

@Composable
private fun DnsLookupScreen() {
    var host by remember { mutableStateOf("example.com") }
    var result by remember { mutableStateOf("—") }
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolHeader("DNS Lookup")
    OutlinedTextField(host, { host = it }, label = { Text("دامنه") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Button(
        onClick = {
            working = true
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    runCatching { InetAddress.getAllByName(host.trim()).joinToString("\n") { it.hostAddress ?: "" } }.getOrElse { "خطا: ${it.message}" }
                }
                working = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !working && host.isNotBlank(),
    ) { Text(if (working) "در حال بررسی…" else "Resolve") }
    ResultCard("نتیجه", result)
}

@Composable
private fun PingScreen() {
    var host by remember { mutableStateOf("8.8.8.8") }
    var result by remember { mutableStateOf("—") }
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolHeader("Ping", "از InetAddress.isReachable استفاده می‌شود؛ برخی شبکه‌ها ICMP را مسدود می‌کنند.")
    OutlinedTextField(host, { host = it }, label = { Text("Host / IP") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Button(
        onClick = {
            working = true
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    runCatching {
                        val started = System.nanoTime()
                        val reachable = InetAddress.getByName(host.trim()).isReachable(3_000)
                        val ms = (System.nanoTime() - started) / 1_000_000.0
                        if (reachable) "پاسخ دریافت شد — ${"%.1f".format(ms)} ms" else "پاسخی دریافت نشد"
                    }.getOrElse { "خطا: ${it.message}" }
                }
                working = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !working,
    ) { Text("آزمایش") }
    ResultCard("نتیجه", result)
}

@Composable
private fun PortTestScreen() {
    var host by remember { mutableStateOf("example.com") }
    var port by remember { mutableStateOf("443") }
    var result by remember { mutableStateOf("—") }
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolHeader("Port Test", "آزمایش اتصال TCP با timeout کوتاه.")
    OutlinedTextField(host, { host = it }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    NumberField("Port", port, { port = it })
    Button(
        onClick = {
            working = true
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    val portNumber = port.toIntOrNull()?.takeIf { it in 1..65535 } ?: return@withContext "پورت نامعتبر"
                    runCatching {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host.trim(), portNumber), 3_000)
                            "پورت $portNumber باز و قابل اتصال است"
                        }
                    }.getOrElse { "اتصال ناموفق: ${it.javaClass.simpleName}" }
                }
                working = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !working,
    ) { Text("بررسی پورت") }
    ResultCard("نتیجه", result)
}

@Composable
private fun WifiInfoScreen() {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    val info = wifiManager.connectionInfo
    ToolHeader("اطلاعات Wi-Fi", "بعضی فیلدها در نسخه‌های جدید Android بدون مجوز Location محدود می‌شوند.")
    ResultCard("SSID", info.ssid ?: "ناموجود")
    ResultCard("Signal", "${info.rssi} dBm", "Link speed: ${info.linkSpeed} Mbps | Frequency: ${info.frequency} MHz")
}

@Composable
private fun WhoisScreen() {
    var domain by remember { mutableStateOf("example.com") }
    var result by remember { mutableStateOf("—") }
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolHeader("WHOIS", "پرس‌وجوی مستقیم TCP روی پورت 43؛ ممکن است برخی شبکه‌ها این پورت را مسدود کنند.")
    OutlinedTextField(domain, { domain = it }, label = { Text("دامنه") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Button(
        onClick = {
            working = true
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    runCatching {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress("whois.iana.org", 43), 4_000)
                            socket.soTimeout = 4_000
                            socket.getOutputStream().bufferedWriter().use { writer ->
                                writer.write(domain.trim())
                                writer.write("\r\n")
                                writer.flush()
                            }
                            socket.getInputStream().bufferedReader().use { reader ->
                                reader.readLines().take(40).joinToString("\n")
                            }
                        }
                    }.getOrElse { "WHOIS ناموفق بود: ${it.javaClass.simpleName}" }
                }
                working = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !working && domain.isNotBlank(),
    ) { Text(if (working) "در حال دریافت…" else "WHOIS") }
    ResultCard("نتیجه", result)
}

@Composable
private fun NetworkSpeedScreen() {
    var downBps by remember { mutableLongStateOf(0L) }
    var upBps by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        var lastTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
        while (isActive) {
            delay(1_000L)
            val rx = TrafficStats.getTotalRxBytes().coerceAtLeast(lastRx)
            val tx = TrafficStats.getTotalTxBytes().coerceAtLeast(lastTx)
            downBps = rx - lastRx
            upBps = tx - lastTx
            lastRx = rx
            lastTx = tx
        }
    }

    ToolHeader("سرعت لحظه‌ای شبکه", "تغییر TrafficStats کل دستگاه در هر ثانیه؛ شامل ترافیک سایر برنامه‌ها نیز می‌شود.")
    ResultCard("دانلود", formatRate(downBps), "آپلود: ${formatRate(upBps)}")
}

@Composable
private fun DataUsageScreen() {
    val rx = TrafficStats.getTotalRxBytes()
    val tx = TrafficStats.getTotalTxBytes()
    ToolHeader("مصرف داده", "TrafficStats از زمان بوت دستگاه؛ بسته به سازنده ممکن است unsupported باشد.")
    ResultCard("دریافت", if (rx >= 0) formatBytes(rx) else "ناموجود", "ارسال: ${if (tx >= 0) formatBytes(tx) else "ناموجود"}")
    if (rx >= 0 && tx >= 0) ResultCard("مجموع", formatBytes(rx + tx))
}

private fun formatRate(bytesPerSecond: Long): String = "${formatBytes(bytesPerSecond)}/s"

private fun formatBytes(bytes: Long): String {
    val value = bytes.coerceAtLeast(0L).toDouble()
    return when {
        value >= 1024 * 1024 * 1024 -> "%.2f GB".format(value / (1024 * 1024 * 1024))
        value >= 1024 * 1024 -> "%.2f MB".format(value / (1024 * 1024))
        value >= 1024 -> "%.2f KB".format(value / 1024)
        else -> "$bytes B"
    }
}
