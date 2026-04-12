package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.SettlementUpiTransaction
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun observeSettlementActivities(groupId: String): Flow<List<SettlementUpiTransaction>>
    suspend fun recordSettlementActivity(groupId: String, activity: SettlementUpiTransaction): Result<Unit>
    suspend fun generateSettlementPdf(
        groupId: String,
        actorUserId: String,
    ): Result<String>
    suspend fun dispatchSettlementPdfToMembers(
        groupId: String,
        actorUserId: String,
        pdfPath: String,
    ): Result<Unit>
    suspend fun markGroupSettled(groupId: String, actorUserId: String): Result<Unit>
}
