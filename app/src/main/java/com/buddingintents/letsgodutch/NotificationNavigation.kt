package com.buddingintents.letsgodutch

import android.net.Uri

data class SettlementOpenRequest(
    val groupId: String,
    val settlementId: String? = null,
)

object NotificationNavigation {
    const val TYPE_SETTLEMENT_PDF: String = "SETTLEMENT_PDF"

    const val EXTRA_NOTIFICATION_TYPE: String = "notification_type"
    const val EXTRA_GROUP_ID: String = "group_id"
    const val EXTRA_SETTLEMENT_ID: String = "settlement_id"

    fun buildSettlementDeepLink(
        groupId: String,
        settlementId: String?,
    ): Uri {
        return Uri.Builder()
            .scheme("letsgodutch")
            .authority("settlement")
            .appendQueryParameter(EXTRA_GROUP_ID, groupId)
            .apply {
                settlementId?.takeIf { it.isNotBlank() }?.let {
                    appendQueryParameter(EXTRA_SETTLEMENT_ID, it)
                }
            }
            .build()
    }
}
