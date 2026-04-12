package com.buddingintents.letsgodutch.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buddingintents.letsgodutch.MainActivity
import com.buddingintents.letsgodutch.R

private const val NOTIFICATION_GROUP_KEY_UPDATES = "letsgodutch_updates_group"
private const val NOTIFICATION_GROUP_SUMMARY_ID = 1001

fun Context.showRealtimeDbNotification(
    title: String,
    body: String,
    notificationIdHint: String?,
) {
    ensureLetsGoDutchNotificationChannel()

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val launchIntent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notificationId = notificationIdHint?.hashCode() ?: System.currentTimeMillis().toInt()
    val builder = NotificationCompat.Builder(this, LetsGoDutchMessagingService.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setGroup(NOTIFICATION_GROUP_KEY_UPDATES)
        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        .setContentIntent(pendingIntent)

    val manager = NotificationManagerCompat.from(this)
    manager.notify(notificationId, builder.build())

    val summaryBuilder = NotificationCompat.Builder(this, LetsGoDutchMessagingService.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.app_name))
        .setContentText("You have new group updates.")
        .setStyle(
            NotificationCompat.InboxStyle()
                .setSummaryText("Group updates"),
        )
        .setGroup(NOTIFICATION_GROUP_KEY_UPDATES)
        .setGroupSummary(true)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
    manager.notify(NOTIFICATION_GROUP_SUMMARY_ID, summaryBuilder.build())
}

private fun Context.ensureLetsGoDutchNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(NotificationManager::class.java) ?: return
    val existing = manager.getNotificationChannel(LetsGoDutchMessagingService.CHANNEL_ID)
    if (existing != null) return

    val channel = NotificationChannel(
        LetsGoDutchMessagingService.CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = getString(R.string.notification_channel_desc)
    }
    manager.createNotificationChannel(channel)
}
