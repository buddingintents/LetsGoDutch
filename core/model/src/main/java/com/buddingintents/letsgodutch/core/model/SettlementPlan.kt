package com.buddingintents.letsgodutch.core.model

import kotlin.math.abs

data class SettlementTransfer(
    val transferKey: String,
    val fromUserId: String,
    val toUserId: String,
    val amountPaise: Long,
)

fun buildSettlementTransfers(balances: List<Balance>): List<SettlementTransfer> {
    val creditors = balances
        .filter { it.netPaise > 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = it.netPaise) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()
    val debtors = balances
        .filter { it.netPaise < 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = abs(it.netPaise)) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()

    val transfers = mutableListOf<SettlementTransfer>()
    var creditorIndex = 0
    var debtorIndex = 0
    var transferIndex = 0
    while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
        val creditor = creditors[creditorIndex]
        val debtor = debtors[debtorIndex]
        val amount = minOf(creditor.amountPaise, debtor.amountPaise)
        if (amount > 0L) {
            transfers += SettlementTransfer(
                transferKey = settlementTransferKey(
                    fromUserId = debtor.userId,
                    toUserId = creditor.userId,
                    amountPaise = amount,
                    index = transferIndex,
                ),
                fromUserId = debtor.userId,
                toUserId = creditor.userId,
                amountPaise = amount,
            )
            transferIndex += 1
        }
        creditor.amountPaise -= amount
        debtor.amountPaise -= amount
        if (creditor.amountPaise <= 0L) creditorIndex += 1
        if (debtor.amountPaise <= 0L) debtorIndex += 1
    }
    return transfers
}

private data class MutableSettlementParty(
    val userId: String,
    var amountPaise: Long,
)
