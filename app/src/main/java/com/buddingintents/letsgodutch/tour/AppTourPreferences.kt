package com.buddingintents.letsgodutch.tour

import android.content.Context

private const val PREFS_NAME = "letsgodutch_app_tour"
private const val KEY_COMPLETED = "completed"

fun Context.isAppTourCompleted(): Boolean {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_COMPLETED, false)
}

fun Context.setAppTourCompleted(completed: Boolean) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_COMPLETED, completed)
        .apply()
}
