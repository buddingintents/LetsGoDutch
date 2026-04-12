package com.buddingintents.letsgodutch.core.model

import java.io.Serializable

enum class SettlementUpiStatus : Serializable {
    SUCCESS,
    CANCELLED,
    PENDING,
    FAILED,
    UNKNOWN,
    ;

    val displayLabel: String
        get() = when (this) {
            SUCCESS -> "Success"
            CANCELLED -> "Cancelled"
            PENDING -> "Pending"
            FAILED -> "Failed"
            UNKNOWN -> "Captured"
        }
}

data class SettlementUpiTransaction(
    val activityId: String = "",
    val transferKey: String,
    val payerUserId: String,
    val payerName: String,
    val receiverUserId: String,
    val receiverName: String,
    val receiverUpiId: String,
    val amountPaise: Long,
    val status: SettlementUpiStatus,
    val paymentAppName: String = "",
    val paymentAppPackageName: String = "",
    val statusConfirmedByUser: Boolean = false,
    val transactionRef: String = "",
    val transactionId: String = "",
    val approvalRefNo: String = "",
    val responseCode: String = "",
    val rawResponse: String = "",
    val handledAtEpochMs: Long = System.currentTimeMillis(),
) : Serializable {
    val bestReference: String
        get() = sequenceOf(transactionRef, approvalRefNo, transactionId)
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

    val excludesFromFinalSettlement: Boolean
        get() = status == SettlementUpiStatus.SUCCESS && !statusConfirmedByUser

    val blocksFurtherUpiInitiation: Boolean
        get() = status == SettlementUpiStatus.SUCCESS
}

fun successfulSettlementTransactions(
    transactions: List<SettlementUpiTransaction>,
): List<SettlementUpiTransaction> {
    return transactions
        .filter { it.excludesFromFinalSettlement }
        .distinctBy { transaction ->
            when {
                transaction.activityId.isNotBlank() -> "activity:${transaction.activityId}"
                transaction.bestReference.isNotBlank() -> "ref:${transaction.bestReference}"
                else -> buildString {
                    append("fallback:")
                    append(transaction.transferKey)
                    append('|')
                    append(transaction.payerUserId)
                    append('|')
                    append(transaction.receiverUserId)
                    append('|')
                    append(transaction.amountPaise)
                    append('|')
                    append(transaction.handledAtEpochMs)
                }
            }
        }
}

fun successfulSettlementTransferKeys(
    transactions: List<SettlementUpiTransaction>,
): Set<String> {
    return transactions
        .filter { it.blocksFurtherUpiInitiation }
        .mapTo(linkedSetOf()) { it.transferKey }
}

fun settlementTransferKey(
    fromUserId: String,
    toUserId: String,
    amountPaise: Long,
    index: Int,
): String = "$fromUserId|$toUserId|$amountPaise|$index"
