package com.buddingintents.letsgodutch.theme

import android.content.Context
import android.content.SharedPreferences
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode

private const val PREFS_NAME = "letsgodutch_theme"
private const val KEY_THEME_MODE = "theme_mode"

fun Context.loadThemeMode(): ThemeMode {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return ThemeMode.fromOrdinal(prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinalValue))
}

fun Context.saveThemeMode(mode: ThemeMode) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_THEME_MODE, mode.ordinalValue)
        .apply()
}
