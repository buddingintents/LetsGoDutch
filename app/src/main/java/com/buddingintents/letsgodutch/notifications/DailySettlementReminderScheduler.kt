package com.buddingintents.letsgodutch.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

object DailySettlementReminderScheduler {
    internal const val ACTION_RUN_DAILY_SETTLEMENT_REMINDER: String =
        "com.buddingintents.letsgodutch.action.RUN_DAILY_SETTLEMENT_REMINDER"

    fun scheduleNext(context: Context, now: ZonedDateTime = ZonedDateTime.now()): Long {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return 0L
        val triggerAtMillis = nextTriggerAtMillis(now)
        val pendingIntent = reminderPendingIntent(context)
        alarmManager.cancel(pendingIntent)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
        return triggerAtMillis
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(reminderPendingIntent(context))
    }

    internal fun nextTriggerAtMillis(now: ZonedDateTime): Long {
        var nextTrigger = now
            .withHour(10)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (!nextTrigger.isAfter(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }
        return nextTrigger.toInstant().toEpochMilli()
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailySettlementReminderReceiver::class.java).apply {
            action = ACTION_RUN_DAILY_SETTLEMENT_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            DAILY_SETTLEMENT_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

private const val DAILY_SETTLEMENT_REMINDER_REQUEST_CODE = 2107
