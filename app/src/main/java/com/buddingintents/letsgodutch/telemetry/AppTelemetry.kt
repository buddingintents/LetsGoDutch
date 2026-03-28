package com.buddingintents.letsgodutch.telemetry

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.buddingintents.letsgodutch.core.model.UserProfile
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AppTelemetry {
    private const val TAG = "LetsGoDutchTelemetry"
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
        FirebaseAnalytics.getInstance(appContext).setAnalyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    fun setUser(profile: UserProfile?) {
        if (!::appContext.isInitialized) return
        val analytics = FirebaseAnalytics.getInstance(appContext)
        val crashlytics = FirebaseCrashlytics.getInstance()
        if (profile == null) {
            analytics.setUserId(null)
            crashlytics.setUserId("anonymous")
            crashlytics.setCustomKey("user_signed_in", false)
            return
        }

        analytics.setUserId(profile.userId)
        val domain = profile.email.substringAfter("@", "").ifBlank { "unknown" }
        val authMode = if (profile.isAnonymous) "anonymous" else "google"
        analytics.setUserProperty("email_domain", domain)
        analytics.setUserProperty("auth_mode", authMode)

        crashlytics.setUserId(profile.userId)
        crashlytics.setCustomKey("user_signed_in", true)
        crashlytics.setCustomKey("user_email_domain", domain)
        crashlytics.setCustomKey("user_is_anonymous", profile.isAnonymous)
    }

    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        if (!::appContext.isInitialized) return
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> bundle.putString(key, value.take(100))
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Float -> bundle.putFloat(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putString(key, value.toString())
                else -> bundle.putString(key, value.toString().take(100))
            }
        }
        runCatching {
            FirebaseAnalytics.getInstance(appContext).logEvent(name, bundle)
        }.onFailure { error ->
            Log.w(TAG, "Unable to log analytics event: $name", error)
        }
    }

    fun recordNonFatal(
        throwable: Throwable,
        tags: Map<String, String> = emptyMap(),
    ) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        tags.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.take(200))
        }
        crashlytics.recordException(throwable)
    }
}
