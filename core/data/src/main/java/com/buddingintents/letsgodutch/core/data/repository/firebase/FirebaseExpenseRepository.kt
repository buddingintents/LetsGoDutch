package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.ExpenseRepository
import com.buddingintents.letsgodutch.core.data.split.SplitCalculator
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseExpenseRepository(
    private val database: FirebaseDatabase,
) : ExpenseRepository {

    private val root = database.reference

    override fun observeExpenses(groupId: String): Flow<List<Expense>> = callbackFlow {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val expensesRef = root.child("expenses").child(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses = snapshot.children.mapNotNull { it.toExpenseOrNull() }
                    .sortedByDescending { it.updatedAtEpochMs }
                trySend(expenses)
                scope.launch {
                    runCatching {
                        recomputeAndPersistBalances(groupId = groupId, expenses = expenses)
                    }.onFailure { throwable ->
                        Log.w("FirebaseExpenseRepo", "observeExpenses failed to recompute balances.", throwable)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseExpenseRepo",
                    "observeExpenses cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }
        expensesRef.addValueEventListener(listener)
        awaitClose {
            expensesRef.removeEventListener(listener)
            scope.cancel()
        }
    }

    override fun observeBalances(groupId: String): Flow<List<Balance>> = callbackFlow {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val balancesRef = root.child("balances").child(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balances = snapshot.children.mapNotNull { it.toBalanceOrNull() }
                    .sortedBy { it.userId }
                trySend(balances)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseExpenseRepo",
                    "observeBalances cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }
        balancesRef.addValueEventListener(listener)
        awaitClose {
            balancesRef.removeEventListener(listener)
            scope.cancel()
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        return runCatching {
            SplitCalculator.allocate(
                totalPaise = expense.amountPaise,
                participantUserIds = expense.participantUserIds,
                splitType = expense.splitType,
                shares = expense.shares,
            ).getOrThrow()

            root.child("expenses")
                .child(expense.groupId)
                .child(expense.expenseId)
                .setValue(expense.toFirebaseMap())
                .await()

            val expenses = loadExpenses(expense.groupId)
            recomputeAndPersistBalances(groupId = expense.groupId, expenses = expenses)
            runCatching {
                enqueueGroupNotification(
                    groupId = expense.groupId,
                    actorUserId = expense.createdByUserId,
                    type = "EXPENSE_ADDED",
                    title = "New expense added",
                    body = "${expense.title} • ${expense.amountPaise.toInrDisplay()}",
                )
            }
        }
    }

    override suspend fun updateExpense(expense: Expense, actorUserId: String): Result<Unit> {
        return runCatching {
            val currentSnapshot = root.child("expenses")
                .child(expense.groupId)
                .child(expense.expenseId)
                .get()
                .await()
            val currentExpense = currentSnapshot.toExpenseOrNull() ?: error("Expense not found.")

            check(canActorModifyExpense(currentExpense, actorUserId)) {
                "Only expense creator or an owner can edit."
            }

            val updatedExpense = expense.copy(updatedAtEpochMs = System.currentTimeMillis())
            SplitCalculator.allocate(
                totalPaise = updatedExpense.amountPaise,
                participantUserIds = updatedExpense.participantUserIds,
                splitType = updatedExpense.splitType,
                shares = updatedExpense.shares,
            ).getOrThrow()

            root.child("expenses")
                .child(updatedExpense.groupId)
                .child(updatedExpense.expenseId)
                .setValue(updatedExpense.toFirebaseMap())
                .await()

            val expenses = loadExpenses(updatedExpense.groupId)
            recomputeAndPersistBalances(groupId = updatedExpense.groupId, expenses = expenses)
            runCatching {
                enqueueGroupNotification(
                    groupId = updatedExpense.groupId,
                    actorUserId = actorUserId,
                    type = "EXPENSE_UPDATED",
                    title = "Expense updated",
                    body = "${updatedExpense.title} • ${updatedExpense.amountPaise.toInrDisplay()}",
                )
            }
        }
    }

    override suspend fun deleteExpense(groupId: String, expenseId: String, actorUserId: String): Result<Unit> {
        return runCatching {
            val currentSnapshot = root.child("expenses")
                .child(groupId)
                .child(expenseId)
                .get()
                .await()
            val currentExpense = currentSnapshot.toExpenseOrNull() ?: error("Expense not found.")

            check(canActorModifyExpense(currentExpense, actorUserId)) {
                "Only expense creator or an owner can delete."
            }

            root.child("expenses")
                .child(groupId)
                .child(expenseId)
                .removeValue()
                .await()

            val expenses = loadExpenses(groupId)
            recomputeAndPersistBalances(groupId = groupId, expenses = expenses)
            runCatching {
                enqueueGroupNotification(
                    groupId = groupId,
                    actorUserId = actorUserId,
                    type = "EXPENSE_DELETED",
                    title = "Expense deleted",
                    body = currentExpense.title,
                )
            }
        }
    }

    private suspend fun canActorModifyExpense(expense: Expense, actorUserId: String): Boolean {
        if (expense.createdByUserId == actorUserId) return true
        val actorRole = root.child("groupMembers")
            .child(expense.groupId)
            .child(actorUserId)
            .child("role")
            .get()
            .await()
            .getValue(String::class.java)
            .orEmpty()
        if (actorRole == "OWNER") return true

        val ownerId = root.child("groups")
            .child(expense.groupId)
            .child("ownerUserId")
            .get()
            .await()
            .getValue(String::class.java)
            .orEmpty()
        return ownerId == actorUserId
    }

    private suspend fun recomputeAndPersistBalances(groupId: String, expenses: List<Expense>) {
        val memberIds = root.child("groupMembers").child(groupId).get().await().children
            .mapNotNull { child ->
                if (child.childBool("active", default = true)) child.key else null
            }
            .toSet()

        val balances = computeBalances(expenses)
            .toMutableMap()
            .apply {
                memberIds.forEach { memberId ->
                    putIfAbsent(memberId, 0L)
                }
            }

        val payload = balances.mapValues { (userId, netPaise) ->
            mapOf(
                "userId" to userId,
                "netPaise" to netPaise,
            )
        }
        root.child("balances").child(groupId).setValue(payload).await()
    }

    private suspend fun loadExpenses(groupId: String): List<Expense> {
        return root.child("expenses").child(groupId).get().await().children
            .mapNotNull { it.toExpenseOrNull() }
    }

    private fun computeBalances(expenses: List<Expense>): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()
        expenses.forEach { expense ->
            val allocation = SplitCalculator.allocate(
                totalPaise = expense.amountPaise,
                participantUserIds = expense.participantUserIds,
                splitType = expense.splitType,
                shares = expense.shares,
            ).getOrElse { return@forEach }

            allocation.forEach { (userId, amount) ->
                balances[userId] = (balances[userId] ?: 0L) - amount
            }
            balances[expense.paidByUserId] = (balances[expense.paidByUserId] ?: 0L) + expense.amountPaise
        }
        return balances
    }

    private suspend fun enqueueGroupNotification(
        groupId: String,
        actorUserId: String,
        type: String,
        title: String,
        body: String,
    ) {
        val members = root.child("groupMembers").child(groupId).get().await().children
            .mapNotNull { snapshot ->
                if (snapshot.childBool("active", default = true)) snapshot.key else null
            }
            .filter { it != actorUserId && !isManualMemberUserId(it) }
        if (members.isEmpty()) return

        val now = System.currentTimeMillis()
        val updates = mutableMapOf<String, Any?>()
        members.forEach { userId ->
            val notificationId = root.child("notifications").child(userId).push().key
                ?: "n_${now}_$userId"
            updates["notifications/$userId/$notificationId"] = mapOf(
                "type" to type,
                "groupId" to groupId,
                "title" to title,
                "body" to body,
                "byUserId" to actorUserId,
                "read" to false,
                "createdAtEpochMs" to now,
            )
        }
        if (updates.isNotEmpty()) {
            root.updateChildren(updates).await()
        }
    }
}

private fun Long.toInrDisplay(): String {
    val abs = kotlin.math.abs(this)
    val rupees = abs / 100
    val paise = abs % 100
    val prefix = if (this < 0) "-INR " else "INR "
    return "$prefix$rupees.${paise.toString().padStart(2, '0')}"
}

private fun isManualMemberUserId(userId: String): Boolean {
    return userId.startsWith("guest_")
}
