package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import kotlinx.coroutines.flow.Flow

interface PersonalExpenseRepository {
    fun observeExpenses(userId: String): Flow<List<PersonalExpenseEntry>>

    suspend fun addExpense(
        userId: String,
        title: String,
        amountPaise: Long,
        spentAtEpochMs: Long = System.currentTimeMillis(),
    ): Result<PersonalExpenseEntry>

    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>
}
