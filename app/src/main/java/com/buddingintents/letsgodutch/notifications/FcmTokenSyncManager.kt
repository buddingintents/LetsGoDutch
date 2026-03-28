package com.buddingintents.letsgodutch.notifications

import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.security.MessageDigest
import java.util.Locale

object FcmTokenSyncManager {
    private val root = FirebaseDatabase.getInstance().reference

    fun syncCurrentTokenForUser(
        userId: String,
        source: String,
    ) {
        if (userId.isBlank()) return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                persistToken(userId = userId, token = token, source = source)
            }
            .addOnFailureListener { error ->
                AppTelemetry.recordNonFatal(
                    error,
                    tags = mapOf(
                        "op" to "fcm_token_fetch",
                        "source" to source,
                        "user_id" to userId,
                    ),
                )
            }
    }

    fun syncProvidedToken(
        userId: String,
        token: String,
        source: String,
    ) {
        if (userId.isBlank() || token.isBlank()) return
        persistToken(userId = userId, token = token, source = source)
    }

    private fun persistToken(
        userId: String,
        token: String,
        source: String,
    ) {
        val tokenId = token.sha256Hex()
        val now = System.currentTimeMillis()
        val payload = mapOf(
            "token" to token,
            "platform" to "android",
            "source" to source,
            "updatedAtEpochMs" to now,
        )

        root.child("fcmTokens")
            .child(userId)
            .child(tokenId)
            .updateChildren(payload)
            .addOnFailureListener { error ->
                AppTelemetry.recordNonFatal(
                    error,
                    tags = mapOf(
                        "op" to "fcm_token_persist",
                        "source" to source,
                        "user_id" to userId,
                    ),
                )
            }
    }
}

private fun String.sha256Hex(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(toByteArray())
    val builder = StringBuilder(hash.size * 2)
    hash.forEach { byte ->
        builder.append(String.format(Locale.US, "%02x", byte))
    }
    return builder.toString()
}
