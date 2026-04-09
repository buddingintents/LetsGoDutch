package com.buddingintents.letsgodutch

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class RecentSettlementRecord(
    val entryId: String,
    val groupId: String,
    val groupName: String,
    val settledAtEpochMs: Long,
    val pdfPath: String,
) {
    val pdfFileName: String
        get() = File(pdfPath).name.ifBlank { "Settlement PDF" }

    val pdfExists: Boolean
        get() = File(pdfPath).exists()
}

class LocalSettlementHistoryStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    fun readSettlements(groupId: String): List<RecentSettlementRecord> {
        val raw = preferences.getString(groupKey(groupId), null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val entryId = item.optString("entryId").trim()
                    val savedGroupId = item.optString("groupId").trim()
                    val groupName = item.optString("groupName").trim()
                    val pdfPath = item.optString("pdfPath").trim()
                    val settledAtEpochMs = item.optLong("settledAtEpochMs")
                    if (entryId.isBlank() || savedGroupId.isBlank() || pdfPath.isBlank()) continue
                    add(
                        RecentSettlementRecord(
                            entryId = entryId,
                            groupId = savedGroupId,
                            groupName = groupName,
                            settledAtEpochMs = settledAtEpochMs,
                            pdfPath = pdfPath,
                        ),
                    )
                }
            }.sortedByDescending { it.settledAtEpochMs }
        }.getOrDefault(emptyList())
    }

    fun addSettlement(record: RecentSettlementRecord) {
        val updated = buildList {
            add(record)
            readSettlements(record.groupId)
                .filterNot { existing -> existing.entryId == record.entryId }
                .take(MAX_ENTRIES - 1)
                .forEach(::add)
        }
        writeSettlements(record.groupId, updated)
    }

    fun deleteSettlement(
        groupId: String,
        entryId: String,
        deletePdf: Boolean = true,
    ): Boolean {
        val existing = readSettlements(groupId)
        val target = existing.firstOrNull { it.entryId == entryId } ?: return false
        if (deletePdf) {
            runCatching { File(target.pdfPath).delete() }
        }
        val updated = existing.filterNot { it.entryId == entryId }
        writeSettlements(groupId, updated)
        return true
    }

    private fun writeSettlements(
        groupId: String,
        records: List<RecentSettlementRecord>,
    ) {
        val payload = JSONArray().apply {
            records
                .sortedByDescending { it.settledAtEpochMs }
                .take(MAX_ENTRIES)
                .forEach { record ->
                    put(
                        JSONObject().apply {
                            put("entryId", record.entryId)
                            put("groupId", record.groupId)
                            put("groupName", record.groupName)
                            put("settledAtEpochMs", record.settledAtEpochMs)
                            put("pdfPath", record.pdfPath)
                        },
                    )
                }
        }
        preferences.edit().putString(groupKey(groupId), payload.toString()).apply()
    }

    private fun groupKey(groupId: String): String = "recent_settlements_$groupId"

    private companion object {
        private const val PREFERENCES_FILE = "local_group_experience"
        private const val MAX_ENTRIES = 20
    }
}

class AppReviewPromptStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    fun recordInteractionAndShouldPrompt(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        if (preferences.getBoolean(KEY_REVIEW_REQUESTED, false)) return false

        val nextCount = preferences.getInt(KEY_INTERACTION_COUNT, 0) + 1
        preferences.edit().putInt(KEY_INTERACTION_COUNT, nextCount).apply()

        val lastPromptAt = preferences.getLong(KEY_LAST_PROMPT_AT_EPOCH_MS, 0L)
        return nextCount >= PROMPT_THRESHOLD && nowEpochMs - lastPromptAt >= PROMPT_COOLDOWN_MS
    }

    fun markPromptShown(nowEpochMs: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong(KEY_LAST_PROMPT_AT_EPOCH_MS, nowEpochMs)
            .putInt(KEY_INTERACTION_COUNT, 0)
            .apply()
    }

    fun markReviewRequested(nowEpochMs: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putBoolean(KEY_REVIEW_REQUESTED, true)
            .putLong(KEY_LAST_PROMPT_AT_EPOCH_MS, nowEpochMs)
            .putInt(KEY_INTERACTION_COUNT, 0)
            .apply()
    }

    private companion object {
        private const val PREFERENCES_FILE = "local_group_experience"
        private const val KEY_INTERACTION_COUNT = "review_interaction_count"
        private const val KEY_LAST_PROMPT_AT_EPOCH_MS = "review_last_prompt_at_epoch_ms"
        private const val KEY_REVIEW_REQUESTED = "review_requested"
        private const val PROMPT_THRESHOLD = 3
        private const val PROMPT_COOLDOWN_MS = 14L * 24L * 60L * 60L * 1000L
    }
}
