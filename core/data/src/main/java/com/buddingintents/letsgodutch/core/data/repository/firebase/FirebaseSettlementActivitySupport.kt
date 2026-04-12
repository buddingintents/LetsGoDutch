package com.buddingintents.letsgodutch.core.data.repository.firebase

import com.buddingintents.letsgodutch.core.data.repository.mergeMemberIntoUser
import com.buddingintents.letsgodutch.core.data.split.SplitCalculator
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.SettlementUpiTransaction
import com.buddingintents.letsgodutch.core.model.successfulSettlementTransactions
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

internal suspend fun recomputeAndPersistGroupBalances(
    root: DatabaseReference,
    groupId: String,
) {
    repairGroupOwnerMembershipAndReferences(root = root, groupId = groupId)

    val activeMembers = root.child("groupMembers").child(groupId).get().await().children
        .mapNotNull { it.toMemberOrNull() }
        .filter { it.active }
    val memberIds = activeMembers.map { it.userId }.toSet()
    val aliasMap = buildLegacyAliasMap(activeMembers)
    val memberById = activeMembers.associateBy { it.userId }
    val rawExpenses = loadGroupExpenses(root = root, groupId = groupId)
    val normalizedExpenses = rawExpenses.normalizeLegacyAliases(aliasMap)
    if (normalizedExpenses != rawExpenses) {
        root.child("expenses").child(groupId).setValue(
            normalizedExpenses.associate { expense ->
                expense.expenseId to expense.toFirebaseMap()
            },
        ).await()
    }
    val rawSettlementActivities = loadSettlementActivities(root = root, groupId = groupId)
    val normalizedSettlementActivities = rawSettlementActivities.normalizeLegacyAliases(
        aliasMap = aliasMap,
        memberById = memberById,
    )
    if (normalizedSettlementActivities != rawSettlementActivities) {
        root.child("settlementActivities").child(groupId).setValue(
            normalizedSettlementActivities.associate { activity ->
                activity.activityId to activity.toFirebaseMap()
            },
        ).await()
    }

    val balances = computeGroupBalances(
        expenses = normalizedExpenses,
        settlementActivities = normalizedSettlementActivities,
    ).toMutableMap()
        .apply {
            memberIds.forEach { memberId ->
                putIfAbsent(memberId, 0L)
            }
        }

    val payload = balances.mapValues { (_, netPaise) ->
        mapOf("netPaise" to netPaise)
    }
    root.child("balances").child(groupId).setValue(payload).await()
}

private fun List<Expense>.normalizeLegacyAliases(
    aliasMap: Map<String, String>,
): List<Expense> {
    if (aliasMap.isEmpty()) return this
    return map { expense ->
        aliasMap.entries.fold(expense) { currentExpense, (legacyAliasUserId, canonicalUserId) ->
            currentExpense.mergeMemberIntoUser(
                fromUserId = legacyAliasUserId,
                toUserId = canonicalUserId,
            )
        }
    }
}

private fun List<SettlementUpiTransaction>.normalizeLegacyAliases(
    aliasMap: Map<String, String>,
    memberById: Map<String, com.buddingintents.letsgodutch.core.model.Member>,
): List<SettlementUpiTransaction> {
    if (aliasMap.isEmpty()) return this
    return map { activity ->
        aliasMap.entries.fold(activity) { currentActivity, (legacyAliasUserId, canonicalUserId) ->
            currentActivity.mergeMemberIntoUser(
                fromUserId = legacyAliasUserId,
                toUserId = canonicalUserId,
                mergedDisplayName = memberById[canonicalUserId]?.displayName.orEmpty(),
            )
        }
    }
}

internal suspend fun loadGroupExpenses(
    root: DatabaseReference,
    groupId: String,
): List<Expense> {
    return root.child("expenses").child(groupId).get().await().children
        .mapNotNull { it.toExpenseOrNull() }
}

internal suspend fun loadSettlementActivities(
    root: DatabaseReference,
    groupId: String,
): List<SettlementUpiTransaction> {
    return root.child("settlementActivities").child(groupId).get().await().children
        .mapNotNull { it.toSettlementUpiTransactionOrNull() }
}

internal fun computeGroupBalances(
    expenses: List<Expense>,
    settlementActivities: List<SettlementUpiTransaction>,
): Map<String, Long> {
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

    successfulSettlementTransactions(settlementActivities).forEach { activity ->
        if (activity.payerUserId.isBlank() || activity.receiverUserId.isBlank()) return@forEach
        balances[activity.payerUserId] = (balances[activity.payerUserId] ?: 0L) + activity.amountPaise
        balances[activity.receiverUserId] = (balances[activity.receiverUserId] ?: 0L) - activity.amountPaise
    }
    return balances
}
