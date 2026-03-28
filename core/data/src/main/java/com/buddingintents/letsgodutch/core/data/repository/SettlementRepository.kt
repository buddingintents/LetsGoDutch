package com.buddingintents.letsgodutch.core.data.repository

interface SettlementRepository {
    suspend fun generateSettlementPdf(groupId: String, actorUserId: String): Result<String>
    suspend fun dispatchSettlementPdfToMembers(
        groupId: String,
        actorUserId: String,
        pdfPath: String,
    ): Result<Unit>
    suspend fun markGroupSettled(groupId: String, actorUserId: String): Result<Unit>
}
