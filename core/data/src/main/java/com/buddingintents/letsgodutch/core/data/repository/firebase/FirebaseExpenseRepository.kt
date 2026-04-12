package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.ExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberBalanceIntoUser
import com.buddingintents.letsgodutch.core.data.split.SplitCalculator
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.GroupActivityType
import com.buddingintents.letsgodutch.core.model.formatIndianCurrency
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
                        recomputeAndPersistGroupBalances(root = root, groupId = groupId)
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
                scope.launch {
                    val activeMembers = runCatching {
                        root.child("groupMembers").child(groupId).get().await().children
                            .mapNotNull { it.toMemberOrNull() }
                            .filter { it.active }
                    }.getOrDefault(emptyList())
                    val activeMemberIds = activeMembers.map { it.userId }.toSet()
                    val legacyAliasUserIds = buildLegacyAliasMap(activeMembers).keys
                    val rawBalances = snapshot.children.mapNotNull { it.toBalanceOrNull() }
                    val requiresRepair = rawBalances.any { balance ->
                        balance.userId !in activeMemberIds || balance.userId in legacyAliasUserIds
                    }
                    val effectiveSnapshot = if (requiresRepair) {
                        runCatching {
                            recomputeAndPersistGroupBalances(root = root, groupId = groupId)
                            balancesRef.get().await()
                        }.onFailure { throwable ->
                            Log.w(
                                "FirebaseExpenseRepo",
                                "observeBalances failed to self-heal stale balance rows.",
                                throwable,
                            )
                        }.getOrDefault(snapshot)
                    } else {
                        snapshot
                    }

                    var normalizedBalanceMap = effectiveSnapshot.children
                        .mapNotNull { it.toBalanceOrNull() }
                        .associate { balance -> balance.userId to balance.netPaise }
                    buildLegacyAliasMap(activeMembers).forEach { (legacyAliasUserId, canonicalUserId) ->
                        normalizedBalanceMap = normalizedBalanceMap.mergeMemberBalanceIntoUser(
                            fromUserId = legacyAliasUserId,
                            toUserId = canonicalUserId,
                        )
                    }
                    val balances = normalizedBalanceMap.entries.map { (userId, netPaise) ->
                        Balance(userId = userId, netPaise = netPaise)
                    }
                        .filter { balance ->
                            balance.userId in activeMemberIds || balance.netPaise != 0L
                        }
                        .sortedBy { it.userId }
                    trySend(balances)
                }
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

            recomputeAndPersistGroupBalances(root = root, groupId = expense.groupId)
            runCatching {
                val actorName = resolveActivityDisplayName(root, expense.groupId, expense.createdByUserId)
                appendGroupActivity(
                    root = root,
                    groupId = expense.groupId,
                    type = GroupActivityType.EXPENSE_ADDED,
                    actorUserId = expense.createdByUserId,
                    actorName = actorName,
                    title = "$actorName added ${expense.title}",
                    detail = "${expense.amountPaise.toInrDisplay()} • ${expense.category.displayLabel}",
                    createdAtEpochMs = expense.updatedAtEpochMs,
                )
            }.onFailure { throwable ->
                Log.w("FirebaseExpenseRepo", "addExpense failed to append activity.", throwable)
            }
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

            recomputeAndPersistGroupBalances(root = root, groupId = updatedExpense.groupId)
            runCatching {
                val actorName = resolveActivityDisplayName(root, updatedExpense.groupId, actorUserId)
                appendGroupActivity(
                    root = root,
                    groupId = updatedExpense.groupId,
                    type = GroupActivityType.EXPENSE_UPDATED,
                    actorUserId = actorUserId,
                    actorName = actorName,
                    title = "$actorName updated ${updatedExpense.title}",
                    detail = "${updatedExpense.amountPaise.toInrDisplay()} • ${updatedExpense.category.displayLabel}",
                    createdAtEpochMs = updatedExpense.updatedAtEpochMs,
                )
            }.onFailure { throwable ->
                Log.w("FirebaseExpenseRepo", "updateExpense failed to append activity.", throwable)
            }
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
            val hasRecordedPayments = root.child("settlementActivities")
                .child(groupId)
                .get()
                .await()
                .hasChildren()
            check(!hasRecordedPayments) {
                "Expenses cannot be deleted after payment activity starts. Settle this group first."
            }

            root.child("expenses")
                .child(groupId)
                .child(expenseId)
                .removeValue()
                .await()

            recomputeAndPersistGroupBalances(root = root, groupId = groupId)
            runCatching {
                val actorName = resolveActivityDisplayName(root, groupId, actorUserId)
                appendGroupActivity(
                    root = root,
                    groupId = groupId,
                    type = GroupActivityType.EXPENSE_DELETED,
                    actorUserId = actorUserId,
                    actorName = actorName,
                    title = "$actorName deleted ${currentExpense.title}",
                    detail = "${currentExpense.amountPaise.toInrDisplay()} • ${currentExpense.category.displayLabel}",
                )
            }.onFailure { throwable ->
                Log.w("FirebaseExpenseRepo", "deleteExpense failed to append activity.", throwable)
            }
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
    return formatIndianCurrency(this, currencyPrefix = "INR ")
}

private fun isManualMemberUserId(userId: String): Boolean {
    return userId.startsWith("guest_")
}
