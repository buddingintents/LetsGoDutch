package com.buddingintents.letsgodutch

import android.content.Context
import android.content.Intent
import android.net.Uri

internal const val PLAY_STORE_DOWNLOAD_URL =
    "https://play.google.com/store/apps/details?id=com.buddingintents.letsgodutch"

internal fun Context.openPlayStoreInlineInstall(source: String) {
    val webUri = Uri.parse("https://play.google.com/d")
        .buildUpon()
        .appendQueryParameter("id", packageName)
        .appendQueryParameter("referrer", "utm_source=$source")
        .build()

    val inlineIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
        setPackage("com.android.vending")
        putExtra("overlay", true)
        putExtra("callerId", packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName"),
    ).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_DOWNLOAD_URL)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    when {
        inlineIntent.resolveActivity(packageManager) != null -> startActivity(inlineIntent)
        marketIntent.resolveActivity(packageManager) != null -> startActivity(marketIntent)
        else -> startActivity(webIntent)
    }
}

internal fun Context.isInstalledFromGooglePlay(): Boolean {
    return runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName == "com.android.vending"
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName) == "com.android.vending"
        }
    }.getOrDefault(false)
}
