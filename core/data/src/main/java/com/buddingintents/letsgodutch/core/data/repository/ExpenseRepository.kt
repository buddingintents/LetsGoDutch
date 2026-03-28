package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(groupId: String): Flow<List<Expense>>
    fun observeBalances(groupId: String): Flow<List<Balance>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense, actorUserId: String): Result<Unit>
    suspend fun deleteExpense(groupId: String, expenseId: String, actorUserId: String): Result<Unit>
}
