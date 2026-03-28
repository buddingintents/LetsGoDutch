package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.PersonalExpenseRepository
import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

class FirebasePersonalExpenseRepository(
    private val database: FirebaseDatabase,
) : PersonalExpenseRepository {

    private val root = database.reference

    override fun observeExpenses(userId: String): Flow<List<PersonalExpenseEntry>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val expensesRef = root.child("personalExpenses").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses = snapshot.children
                    .mapNotNull { it.toPersonalExpenseOrNull() }
                    .sortedByDescending { it.spentAtEpochMs }
                trySend(expenses)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebasePersonalRepo",
                    "observeExpenses cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }
        expensesRef.addValueEventListener(listener)
        awaitClose { expensesRef.removeEventListener(listener) }
    }.conflate()

    override suspend fun addExpense(
        userId: String,
        title: String,
        amountPaise: Long,
        spentAtEpochMs: Long,
    ): Result<PersonalExpenseEntry> {
        return runCatching {
            require(userId.isNotBlank()) { "User id is required." }
            val normalizedTitle = title.trim()
            require(normalizedTitle.isNotBlank()) { "Expense title is required." }
            require(amountPaise > 0L) { "Amount must be greater than zero." }

            val now = System.currentTimeMillis()
            val expenseId = root.child("personalExpenses").child(userId).push().key ?: "pex_$now"
            val expense = PersonalExpenseEntry(
                expenseId = expenseId,
                userId = userId,
                title = normalizedTitle,
                amountPaise = amountPaise,
                spentAtEpochMs = spentAtEpochMs,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
            )
            root.child("personalExpenses").child(userId).child(expenseId).setValue(expense.toFirebaseMap()).await()
            expense
        }
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        return runCatching {
            require(userId.isNotBlank()) { "User id is required." }
            require(expenseId.isNotBlank()) { "Expense id is required." }
            root.child("personalExpenses").child(userId).child(expenseId).removeValue().await()
        }
    }
}

private fun DataSnapshot.toPersonalExpenseOrNull(): PersonalExpenseEntry? {
    val expenseId = key ?: return null
    val userId = childString("userId")
    val title = childString("title")
    if (userId.isBlank() || title.isBlank()) return null

    return PersonalExpenseEntry(
        expenseId = expenseId,
        userId = userId,
        title = title,
        amountPaise = childLong("amountPaise"),
        spentAtEpochMs = childLong("spentAtEpochMs"),
        createdAtEpochMs = childLong("createdAtEpochMs"),
        updatedAtEpochMs = childLong("updatedAtEpochMs"),
    )
}

private fun PersonalExpenseEntry.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "expenseId" to expenseId,
        "userId" to userId,
        "title" to title,
        "amountPaise" to amountPaise,
        "spentAtEpochMs" to spentAtEpochMs,
        "createdAtEpochMs" to createdAtEpochMs,
        "updatedAtEpochMs" to updatedAtEpochMs,
    )
}
