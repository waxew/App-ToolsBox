package com.asteam.toolbox.data

import android.content.Context
import android.net.Uri

/**
 * Small on-device preferences store. Profile, favorites and the persistent
 * counter survive normal application updates without requiring an account.
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

    private companion object {
        const val KEY_NAME = "profile_name"
        const val KEY_PROFILE_IMAGE = "profile_image_uri"
        const val KEY_FAVORITES = "favorites"
        const val KEY_COUNTER = "counter"
    }
}
