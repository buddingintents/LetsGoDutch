package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val expenseId: String,
    val groupId: String,
    val title: String,
    val amountPaise: Long,
    // Stored as dd-MM-yyyy in backend, displayed in UI as dd-MMM.
    val paymentDate: String = "",
    val currencyCode: String = "INR",
    val paidByUserId: String,
    val participantUserIds: List<String>,
    val splitType: SplitType,
    val shares: List<SplitShare>,
    val createdByUserId: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
