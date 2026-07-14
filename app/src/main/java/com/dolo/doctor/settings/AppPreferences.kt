package com.dolo.doctor.settings

import android.content.SharedPreferences

class AppPreferences(private val preferences: SharedPreferences) {
    fun isDarkTheme(): Boolean = preferences.getBoolean(KEY_DARK_THEME, false)
    fun setDarkTheme(enabled: Boolean): Boolean = preferences.edit().putBoolean(KEY_DARK_THEME, enabled).commit()

    private companion object {
        const val KEY_DARK_THEME = "dark_theme_enabled"
    }
}