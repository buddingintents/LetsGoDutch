package com.buddingintents.letsgodutch.notifications

import android.content.Context

object NotificationDisplayDeduper {
    private const val PREFS_NAME = "notification_display_deduper"
    private const val RETAIN_MS = 10 * 60 * 1000L

    @Synchronized
    fun tryAcquire(
        context: Context,
        notificationId: String,
    ): Boolean {
        if (notificationId.isBlank()) return true

        val now = System.currentTimeMillis()
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getLong(notificationId, 0L)
        if (existing > 0L && now - existing < RETAIN_MS) {
            return false
        }

        val editor = prefs.edit()
        prefs.all.forEach { (key, value) ->
            val timestamp = value as? Long ?: return@forEach
            if (now - timestamp >= RETAIN_MS) {
                editor.remove(key)
            }
        }
        editor.putLong(notificationId, now)
        editor.apply()
        return true
    }
}
