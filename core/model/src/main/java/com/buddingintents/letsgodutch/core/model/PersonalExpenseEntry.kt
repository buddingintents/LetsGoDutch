package com.buddingintents.letsgodutch.core.model

data class PersonalExpenseEntry(
    val expenseId: String,
    val userId: String,
    val title: String,
    val amountPaise: Long,
    val spentAtEpochMs: Long,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
