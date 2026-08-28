package com.asteam.toolbox.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small on-device preferences store. Profile, favorites, counter and scanner
 * history survive normal application updates without requiring an account.
 */
class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("toolsbox_preferences", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_NAME, "کاربر جعبه ابزار") ?: "کاربر جعبه ابزار"
        set(value) = prefs.edit().putString(KEY_NAME, value.trim()).apply()

    var profileImage: String?
        get() = prefs.getString(KEY_PROFILE_IMAGE, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_IMAGE, value).apply()

    var counter: Int
        get() = prefs.getInt(KEY_COUNTER, 0)
        set(value) = prefs.edit().putInt(KEY_COUNTER, value).apply()

    fun toggleFavorite(toolId: String) {
        val next = favorites().toMutableSet()
        if (!next.add(toolId)) next.remove(toolId)
        prefs.edit().putStringSet(KEY_FAVORITES, next).apply()
    }

    fun favorites(): Set<String> =
        prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()

    fun persistProfileImageUri(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        profileImage = uri.toString()
    }

    fun scanHistory(): List<ScanHistoryItem> {
        val raw = prefs.getString(KEY_SCAN_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        ScanHistoryItem(
                            value = item.optString("value"),
                            format = item.optString("format", "UNKNOWN"),
                            scannedAt = item.optLong("scannedAt"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun addScanHistory(value: String, format: String, scannedAt: Long = System.currentTimeMillis()) {
        if (value.isBlank()) return
        val current = scanHistory().toMutableList()
        current.removeAll { it.value == value && it.format == format }
        current.add(0, ScanHistoryItem(value = value, format = format, scannedAt = scannedAt))
        saveScanHistory(current.take(MAX_SCAN_HISTORY))
    }

    /** Removes exactly one stored scan without affecting other history entries. */
    fun removeScanHistory(item: ScanHistoryItem) {
        val next = scanHistory().filterNot {
            it.value == item.value && it.format == item.format && it.scannedAt == item.scannedAt
        }
        saveScanHistory(next)
    }

    fun clearScanHistory() {
        prefs.edit().remove(KEY_SCAN_HISTORY).apply()
    }

    private fun saveScanHistory(items: List<ScanHistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("value", item.value)
                    .put("format", item.format)
                    .put("scannedAt", item.scannedAt),
            )
        }
        prefs.edit().putString(KEY_SCAN_HISTORY, array.toString()).apply()
    }

    private companion object {
        const val KEY_NAME = "profile_name"
        const val KEY_PROFILE_IMAGE = "profile_image_uri"
        const val KEY_FAVORITES = "favorites"
        const val KEY_COUNTER = "counter"
        const val KEY_SCAN_HISTORY = "scan_history"
        const val MAX_SCAN_HISTORY = 100
    }
}

data class ScanHistoryItem(
    val value: String,
    val format: String,
    val scannedAt: Long,
)
