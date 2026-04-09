package com.buddingintents.letsgodutch.core.model

import kotlin.math.abs

enum class SettlementState {
    RECEIVABLE,
    PAYABLE,
    SETTLED,
}

data class SettlementSummary(
    val netPaise: Long,
) {
    val state: SettlementState = when {
        netPaise > 0L -> SettlementState.RECEIVABLE
        netPaise < 0L -> SettlementState.PAYABLE
        else -> SettlementState.SETTLED
    }
    val amountPaise: Long = abs(netPaise)
    val amount: Money = Money(amountPaise)
    val label: String = when (state) {
        SettlementState.RECEIVABLE -> "They owe you"
        SettlementState.PAYABLE -> "You owe them"
        SettlementState.SETTLED -> "All settled"
    }
    val amountDisplay: String = amount.toRupeeDisplay()
    val hasBalance: Boolean = netPaise != 0L
}

data class UnsettledGroupsSummary(
    val unsettledGroupCount: Int,
    val aggregatedNetPaise: Long,
    val totalReceivablePaise: Long,
    val totalPayablePaise: Long,
) {
    val settlement: SettlementSummary = SettlementSummary(aggregatedNetPaise)
    val hasMixedBalances: Boolean = unsettledGroupCount > 0 &&
        aggregatedNetPaise == 0L &&
        totalReceivablePaise > 0L &&
        totalPayablePaise > 0L
}

fun summarizeGroupNetBalances(groupNetPaiseById: Map<String, Long>): UnsettledGroupsSummary {
    val unsettledBalances = groupNetPaiseById.values.filter { it != 0L }
    return UnsettledGroupsSummary(
        unsettledGroupCount = unsettledBalances.size,
        aggregatedNetPaise = groupNetPaiseById.values.sum(),
        totalReceivablePaise = groupNetPaiseById.values.filter { it > 0L }.sum(),
        totalPayablePaise = groupNetPaiseById.values.filter { it < 0L }.sumOf(::abs),
    )
}
