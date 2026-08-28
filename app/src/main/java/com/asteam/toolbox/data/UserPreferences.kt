package com.asteam.toolbox.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device preference store.
 * Stable keys keep user data compatible across normal application upgrades.
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

    /** system / light / dark */
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    /** blue / green / orange / purple */
    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, "blue") ?: "blue"
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    /** grid / list */
    var homeLayout: String
        get() = prefs.getString(KEY_HOME_LAYOUT, "grid") ?: "grid"
        set(value) = prefs.edit().putString(KEY_HOME_LAYOUT, value).apply()

    /** catalog / title / recent */
    var sortMode: String
        get() = prefs.getString(KEY_SORT_MODE, "catalog") ?: "catalog"
        set(value) = prefs.edit().putString(KEY_SORT_MODE, value).apply()

    /** compact / normal / large */
    var cardSize: String
        get() = prefs.getString(KEY_CARD_SIZE, "normal") ?: "normal"
        set(value) = prefs.edit().putString(KEY_CARD_SIZE, value).apply()

    fun toggleFavorite(toolId: String) {
        val next = favorites().toMutableSet()
        if (!next.add(toolId)) next.remove(toolId)
        prefs.edit().putStringSet(KEY_FAVORITES, next).apply()
    }

    fun favorites(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()

    fun hiddenTools(): Set<String> = prefs.getStringSet(KEY_HIDDEN_TOOLS, emptySet())?.toSet() ?: emptySet()

    fun setToolHidden(toolId: String, hidden: Boolean) {
        val next = hiddenTools().toMutableSet()
        if (hidden) next.add(toolId) else next.remove(toolId)
        prefs.edit().putStringSet(KEY_HIDDEN_TOOLS, next).apply()
    }

    /** A user-curated collection separate from Favorites. */
    fun customCollection(): Set<String> = prefs.getStringSet(KEY_CUSTOM_COLLECTION, emptySet())?.toSet() ?: emptySet()

    fun toggleCustomCollection(toolId: String) {
        val next = customCollection().toMutableSet()
        if (!next.add(toolId)) next.remove(toolId)
        prefs.edit().putStringSet(KEY_CUSTOM_COLLECTION, next).apply()
    }

    fun recentTools(): List<String> = prefs.getString(KEY_RECENT_TOOLS, "").orEmpty().split('|').filter(String::isNotBlank)

    fun markToolOpened(toolId: String) {
        val next = recentTools().filterNot { it == toolId }.toMutableList()
        next.add(0, toolId)
        prefs.edit().putString(KEY_RECENT_TOOLS, next.take(20).joinToString("|")).apply()
    }

    fun persistProfileImageUri(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                    add(ScanHistoryItem(item.optString("value"), item.optString("format", "UNKNOWN"), item.optLong("scannedAt")))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun addScanHistory(value: String, format: String, scannedAt: Long = System.currentTimeMillis()) {
        if (value.isBlank()) return
        val current = scanHistory().toMutableList()
        current.removeAll { it.value == value && it.format == format }
        current.add(0, ScanHistoryItem(value, format, scannedAt))
        saveScanHistory(current.take(MAX_SCAN_HISTORY))
    }

    fun removeScanHistory(item: ScanHistoryItem) {
        saveScanHistory(scanHistory().filterNot { it == item })
    }

    fun clearScanHistory() { prefs.edit().remove(KEY_SCAN_HISTORY).apply() }

    /** Portable local backup. Profile image URI is excluded because it is device/provider specific. */
    fun exportBackupJson(): String {
        val history = JSONArray()
        scanHistory().forEach { item ->
            history.put(JSONObject().put("value", item.value).put("format", item.format).put("scannedAt", item.scannedAt))
        }
        return JSONObject()
            .put("schemaVersion", 2)
            .put("userName", userName)
            .put("counter", counter)
            .put("themeMode", themeMode)
            .put("accentColor", accentColor)
            .put("homeLayout", homeLayout)
            .put("sortMode", sortMode)
            .put("cardSize", cardSize)
            .put("favorites", JSONArray(favorites().toList()))
            .put("hiddenTools", JSONArray(hiddenTools().toList()))
            .put("customCollection", JSONArray(customCollection().toList()))
            .put("recentTools", JSONArray(recentTools()))
            .put("scanHistory", history)
            .toString(2)
    }

    /** Imports schema 1 or 2; unknown future schemas are rejected rather than partially imported. */
    fun importBackupJson(raw: String): Result<Unit> = runCatching {
        val root = JSONObject(raw)
        val schema = root.optInt("schemaVersion", 0)
        require(schema in 1..2) { "Unsupported backup schema" }

        fun jsonSet(name: String): Set<String> {
            val array = root.optJSONArray(name) ?: JSONArray()
            return buildSet { for (i in 0 until array.length()) add(array.optString(i)) }.filter(String::isNotBlank).toSet()
        }

        val editor = prefs.edit()
            .putString(KEY_NAME, root.optString("userName", userName))
            .putInt(KEY_COUNTER, root.optInt("counter", counter))
            .putString(KEY_THEME_MODE, root.optString("themeMode", "system"))
            .putString(KEY_ACCENT_COLOR, root.optString("accentColor", "blue"))
            .putString(KEY_HOME_LAYOUT, root.optString("homeLayout", "grid"))
            .putString(KEY_SORT_MODE, root.optString("sortMode", "catalog"))
            .putString(KEY_CARD_SIZE, root.optString("cardSize", "normal"))
            .putStringSet(KEY_FAVORITES, jsonSet("favorites"))
            .putStringSet(KEY_HIDDEN_TOOLS, jsonSet("hiddenTools"))
            .putStringSet(KEY_CUSTOM_COLLECTION, jsonSet("customCollection"))

        val recentArray = root.optJSONArray("recentTools") ?: JSONArray()
        val recent = buildList { for (i in 0 until recentArray.length()) add(recentArray.optString(i)) }
            .filter(String::isNotBlank).take(20)
        editor.putString(KEY_RECENT_TOOLS, recent.joinToString("|"))
        root.optJSONArray("scanHistory")?.let { editor.putString(KEY_SCAN_HISTORY, it.toString()) }
        editor.apply()
    }

    private fun saveScanHistory(items: List<ScanHistoryItem>) {
        val array = JSONArray()
        items.forEach { array.put(JSONObject().put("value", it.value).put("format", it.format).put("scannedAt", it.scannedAt)) }
        prefs.edit().putString(KEY_SCAN_HISTORY, array.toString()).apply()
    }

    private companion object {
        const val KEY_NAME = "profile_name"
        const val KEY_PROFILE_IMAGE = "profile_image_uri"
        const val KEY_FAVORITES = "favorites"
        const val KEY_COUNTER = "counter"
        const val KEY_SCAN_HISTORY = "scan_history"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_HOME_LAYOUT = "home_layout"
        const val KEY_SORT_MODE = "sort_mode"
        const val KEY_CARD_SIZE = "card_size"
        const val KEY_HIDDEN_TOOLS = "hidden_tools"
        const val KEY_CUSTOM_COLLECTION = "custom_collection"
        const val KEY_RECENT_TOOLS = "recent_tools"
        const val MAX_SCAN_HISTORY = 100
    }
}

data class ScanHistoryItem(val value: String, val format: String, val scannedAt: Long)
