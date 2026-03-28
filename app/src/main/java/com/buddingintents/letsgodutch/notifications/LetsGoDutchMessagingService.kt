package com.buddingintents.letsgodutch.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buddingintents.letsgodutch.MainActivity
import com.buddingintents.letsgodutch.R
import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class LetsGoDutchMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("LetsGoDutchFCM", "Refreshed token: $token")
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (userId.isNotBlank()) {
            FcmTokenSyncManager.syncProvidedToken(
                userId = userId,
                token = token,
                source = "on_new_token",
            )
        }
        AppTelemetry.logEvent(
            name = "fcm_token_refreshed",
            params = mapOf("has_user" to userId.isNotBlank()),
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("LetsGoDutchFCM", "Message received: ${message.data}")
        showLocalNotification(message)
        AppTelemetry.logEvent(
            name = "fcm_message_received",
            params = mapOf(
                "has_data" to message.data.isNotEmpty(),
                "has_notification" to (message.notification != null),
            ),
        )
    }

    private fun showLocalNotification(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new group update."

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        ensureNotificationChannel()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_GROUP_KEY_UPDATES)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setContentIntent(pendingIntent)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = NotificationManagerCompat.from(this)
        manager.notify(Random.nextInt(), builder.build())
        val summaryBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID: String = "letsgodutch_updates"
        private const val NOTIFICATION_GROUP_KEY_UPDATES = "letsgodutch_updates_group"
        private const val NOTIFICATION_GROUP_SUMMARY_ID = 1001
    }
}
