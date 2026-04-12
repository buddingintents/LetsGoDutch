package com.buddingintents.letsgodutch.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buddingintents.letsgodutch.MainActivity
import com.buddingintents.letsgodutch.R
import com.buddingintents.letsgodutch.core.model.SettlementState
import com.buddingintents.letsgodutch.core.model.UnsettledGroupsSummary
import com.buddingintents.letsgodutch.core.model.summarizeGroupNetBalances
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DailySettlementReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        when (intent.action) {
            DailySettlementReminderScheduler.ACTION_RUN_DAILY_SETTLEMENT_REMINDER -> {
                DailySettlementReminderScheduler.scheduleNext(appContext)
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    runCatching {
                        refreshReminder(appContext)
                    }.onFailure { throwable ->
                        Log.w(TAG, "Unable to refresh daily settlement reminder.", throwable)
                    }
                    pendingResult.finish()
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_TIME_SET,
            -> {
                DailySettlementReminderScheduler.scheduleNext(appContext)
            }
        }
    }

    private suspend fun refreshReminder(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (userId.isBlank()) {
            notificationManager.cancel(DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID)
            return
        }

        val root = FirebaseDatabase.getInstance().reference
        val groupIds = root.child("userGroups")
            .child(userId)
            .get()
            .await()
            .children
            .mapNotNull { it.key }
            .distinct()

        if (groupIds.isEmpty()) {
            notificationManager.cancel(DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID)
            return
        }

        val groupNetPaiseById = mutableMapOf<String, Long>()
        groupIds.forEach { groupId ->
            val groupSnapshot = root.child("groups").child(groupId).get().await()
            if (!groupSnapshot.exists()) return@forEach
            val isActive = groupSnapshot.child("active").getValue(Boolean::class.java) != false
            if (!isActive) return@forEach

            val netPaise = root.child("balances")
                .child(groupId)
                .child(userId)
                .child("netPaise")
                .get()
                .await()
                .getValue(Long::class.java)
                ?: 0L
            groupNetPaiseById[groupId] = netPaise
        }

        val summary = summarizeGroupNetBalances(groupNetPaiseById)
        if (summary.unsettledGroupCount == 0) {
            notificationManager.cancel(DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID)
            return
        }

        postReminderNotification(
            context = context,
            summary = summary,
        )
    }

    private fun postReminderNotification(
        context: Context,
        summary: UnsettledGroupsSummary,
    ) {
        ensureNotificationChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val title = if (summary.unsettledGroupCount == 1) {
            "1 group needs settlement"
        } else {
            "${summary.unsettledGroupCount} groups need settlement"
        }
        val greeting = if (LocalTime.now().hour < 12) "Good morning." else "Hello."
        val body = when {
            summary.hasMixedBalances ->
                "$greeting Some groups owe you and some need payment. Open Let's Go Dutch to review today's settlements."

            summary.settlement.state == SettlementState.RECEIVABLE ||
                summary.settlement.state == SettlementState.PAYABLE ->
                "$greeting ${summary.settlement.label} ${summary.settlement.amountDisplay}."

            else ->
                "$greeting Some groups still need settlement."
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, LetsGoDutchMessagingService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title. $body"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context)
            .notify(DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID, builder.build())
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(LetsGoDutchMessagingService.CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            LetsGoDutchMessagingService.CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val ACTION_TIME_SET = "android.intent.action.TIME_SET"
        private const val TAG = "DailySettlementReminder"
        private const val DAILY_SETTLEMENT_REMINDER_NOTIFICATION_ID = 1207
    }
}
