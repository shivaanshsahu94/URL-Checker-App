package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("url_checker_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_AVATAR = "profile_avatar"
        private const val KEY_DEFAULT_CHECKER = "default_checker"
        private const val KEY_THEME = "ui_theme"
        private const val KEY_SAVE_HISTORY = "save_history"
        private const val KEY_AUTO_DELETE_DAYS = "auto_delete_days"
    }

    var profileName: String
        get() = prefs.getString(KEY_PROFILE_NAME, "User") ?: "User"
        set(value) = prefs.edit().putString(KEY_PROFILE_NAME, value).apply()

    var profileAvatar: Int
        get() = prefs.getInt(KEY_PROFILE_AVATAR, 0)
        set(value) = prefs.edit().putInt(KEY_PROFILE_AVATAR, value).apply()

    var defaultChecker: String
        get() = prefs.getString(KEY_DEFAULT_CHECKER, "VirusTotal") ?: "VirusTotal"
        set(value) = prefs.edit().putString(KEY_DEFAULT_CHECKER, value).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, "System") ?: "System"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var saveHistory: Boolean
        get() = prefs.getBoolean(KEY_SAVE_HISTORY, true)
        set(value) = prefs.edit().putBoolean(KEY_SAVE_HISTORY, value).apply()

    var autoDeleteDays: Int
        get() = prefs.getInt(KEY_AUTO_DELETE_DAYS, 30)
        set(value) = prefs.edit().putInt(KEY_AUTO_DELETE_DAYS, value).apply()
}
