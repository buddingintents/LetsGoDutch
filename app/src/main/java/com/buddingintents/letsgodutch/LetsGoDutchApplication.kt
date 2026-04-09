package com.buddingintents.letsgodutch

import android.app.Application
import com.buddingintents.letsgodutch.notifications.DailySettlementReminderScheduler
import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.ads.MobileAds

class LetsGoDutchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppTelemetry.initialize(this)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        MobileAds.initialize(this)
        DailySettlementReminderScheduler.scheduleNext(this)
        AppTelemetry.logEvent("app_started")
        runCatching {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
    }
}
