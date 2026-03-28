package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AnonymousSessionSecureStore(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = createPrefs(appContext)

    private val recentNamesState = MutableStateFlow(readRecentNames())
    val recentNames: StateFlow<List<String>> = recentNamesState.asStateFlow()

    fun markAnonymousSessionHidden(userId: String) {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return
        prefs.edit()
            .putString(KEY_HIDDEN_ANON_USER_ID, normalizedUserId)
            .apply()
    }

    fun clearHiddenAnonymousSession(userId: String? = null) {
        val normalizedUserId = userId?.trim().orEmpty()
        val hiddenUserId = prefs.getString(KEY_HIDDEN_ANON_USER_ID, "").orEmpty().trim()
        if (hiddenUserId.isBlank()) return
        if (normalizedUserId.isNotBlank() && hiddenUserId != normalizedUserId) return
        prefs.edit()
            .remove(KEY_HIDDEN_ANON_USER_ID)
            .apply()
    }

    fun isAnonymousSessionHidden(userId: String): Boolean {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return false
        val hiddenUserId = prefs.getString(KEY_HIDDEN_ANON_USER_ID, "").orEmpty().trim()
        return hiddenUserId == normalizedUserId
    }

    fun recordAnonymousDisplayName(displayName: String) {
        val normalizedName = displayName.trim()
        if (normalizedName.isBlank()) return

        val updated = buildList {
            add(normalizedName)
            recentNamesState.value.forEach { existing ->
                if (!existing.equals(normalizedName, ignoreCase = true)) {
                    add(existing)
                }
            }
        }.take(MAX_RECENT_NAMES)

        recentNamesState.value = updated
        prefs.edit()
            .putString(KEY_RECENT_ANON_DISPLAY_NAMES_JSON, serializeNames(updated))
            .apply()
    }

    private fun readRecentNames(): List<String> {
        val raw = prefs.getString(KEY_RECENT_ANON_DISPLAY_NAMES_JSON, "").orEmpty().trim()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val name = array.optString(index).trim()
                    if (name.isNotBlank()) add(name)
                }
            }.take(MAX_RECENT_NAMES)
        }.getOrDefault(emptyList())
    }

    private fun serializeNames(names: List<String>): String {
        val jsonArray = JSONArray()
        names.take(MAX_RECENT_NAMES).forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    private fun createPrefs(context: Context): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            @Suppress("DEPRECATION")
            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse {
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private companion object {
        private const val SECURE_PREFS_NAME = "letsgodutch_anon_secure_store"
        private const val FALLBACK_PREFS_NAME = "letsgodutch_anon_store_fallback"
        private const val KEY_HIDDEN_ANON_USER_ID = "hidden_anonymous_user_id"
        private const val KEY_RECENT_ANON_DISPLAY_NAMES_JSON = "recent_anonymous_display_names_json"
        private const val MAX_RECENT_NAMES = 5
    }
}
