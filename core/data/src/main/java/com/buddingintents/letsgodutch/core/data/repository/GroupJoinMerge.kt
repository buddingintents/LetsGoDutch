package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.SplitShare

internal fun Expense.mergeMemberIntoUser(
    fromUserId: String,
    toUserId: String,
): Expense {
    if (fromUserId.isBlank() || toUserId.isBlank() || fromUserId == toUserId) return this

    return copy(
        paidByUserId = paidByUserId.replaceUserId(fromUserId = fromUserId, toUserId = toUserId),
        participantUserIds = participantUserIds.mergeUserIdReferences(
            fromUserId = fromUserId,
            toUserId = toUserId,
        ),
        shares = shares
            .map { share ->
                if (share.userId == fromUserId) {
                    share.copy(userId = toUserId)
                } else {
                    share
                }
            }
            .mergeDuplicateShares(),
        createdByUserId = createdByUserId.replaceUserId(fromUserId = fromUserId, toUserId = toUserId),
    )
}

internal fun Map<String, Long>.mergeMemberBalanceIntoUser(
    fromUserId: String,
    toUserId: String,
): Map<String, Long> {
    if (fromUserId.isBlank() || toUserId.isBlank() || fromUserId == toUserId) return this

    val merged = toMutableMap()
    val sourceAmount = merged.remove(fromUserId) ?: 0L
    merged[toUserId] = (merged[toUserId] ?: 0L) + sourceAmount
    return merged
}

internal fun Iterable<String>.mergeUserIdReferences(
    fromUserId: String,
    toUserId: String,
): List<String> {
    if (fromUserId.isBlank() || toUserId.isBlank() || fromUserId == toUserId) {
        return distinct()
    }

    return map { value -> value.replaceUserId(fromUserId = fromUserId, toUserId = toUserId) }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun String.replaceUserId(
    fromUserId: String,
    toUserId: String,
): String {
    return if (this == fromUserId) toUserId else this
}

private fun List<SplitShare>.mergeDuplicateShares(): List<SplitShare> {
    if (isEmpty()) return emptyList()

    val mergedByUserId = linkedMapOf<String, MutableList<SplitShare>>()
    forEach { share ->
        val key = share.userId
        mergedByUserId.getOrPut(key) { mutableListOf() }.add(share)
    }

    return mergedByUserId.map { (userId, shares) ->
        SplitShare(
            userId = userId,
            amountPaise = shares.sumLongOrNull { it.amountPaise },
            percentage = shares.sumDoubleOrNull { it.percentage },
            customUnits = shares.sumDoubleOrNull { it.customUnits },
        )
    }
}

private inline fun <T> Iterable<T>.sumLongOrNull(selector: (T) -> Long?): Long? {
    var sum = 0L
    var hasValue = false
    forEach { item ->
        val value = selector(item) ?: return@forEach
        sum += value
        hasValue = true
    }
    return if (hasValue) sum else null
}

private inline fun <T> Iterable<T>.sumDoubleOrNull(selector: (T) -> Double?): Double? {
    var sum = 0.0
    var hasValue = false
    forEach { item ->
        val value = selector(item) ?: return@forEach
        sum += value
        hasValue = true
    }
    return if (hasValue) sum else null
}
