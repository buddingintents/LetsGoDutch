package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.data.repository.firebase.computeGroupBalances
import com.buddingintents.letsgodutch.core.data.repository.firebase.legacyPlaceholderAliasUserId
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.SettlementUpiStatus
import com.buddingintents.letsgodutch.core.model.SettlementUpiTransaction
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import com.buddingintents.letsgodutch.core.model.successfulSettlementTransactions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GroupJoinMergeTest {

    @Test
    fun `mergeMemberIntoUser rewrites expense ids and combines duplicate shares`() {
        val expense = Expense(
            expenseId = "exp_1",
            groupId = "group_1",
            title = "Dinner",
            amountPaise = 4_500L,
            paidByUserId = "guest_123",
            participantUserIds = listOf("guest_123", "user_42", "user_42"),
            splitType = SplitType.EXACT,
            shares = listOf(
                SplitShare(userId = "guest_123", amountPaise = 2_000L),
                SplitShare(userId = "user_42", amountPaise = 1_000L),
                SplitShare(userId = "guest_123", amountPaise = 1_500L),
            ),
            createdByUserId = "guest_123",
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 2L,
        )

        val mergedExpense = expense.mergeMemberIntoUser(
            fromUserId = "guest_123",
            toUserId = "user_42",
        )

        assertEquals("user_42", mergedExpense.paidByUserId)
        assertEquals("user_42", mergedExpense.createdByUserId)
        assertEquals(listOf("user_42"), mergedExpense.participantUserIds)
        assertEquals(
            listOf(
                SplitShare(
                    userId = "user_42",
                    amountPaise = 4_500L,
                ),
            ),
            mergedExpense.shares,
        )
    }

    @Test
    fun `mergeMemberBalanceIntoUser sums source and target balances`() {
        val mergedBalances = mapOf(
            "guest_123" to 1_250L,
            "user_42" to -250L,
            "user_77" to -1_000L,
        ).mergeMemberBalanceIntoUser(
            fromUserId = "guest_123",
            toUserId = "user_42",
        )

        assertEquals(
            mapOf(
                "user_42" to 1_000L,
                "user_77" to -1_000L,
            ),
            mergedBalances,
        )
    }

    @Test
    fun `mergeUserIdReferences replaces source id and keeps distinct order`() {
        val mergedIds = listOf("guest_123", "user_77", "guest_123", "user_42")
            .mergeUserIdReferences(
                fromUserId = "guest_123",
                toUserId = "user_42",
            )

        assertEquals(listOf("user_42", "user_77"), mergedIds)
    }

    @Test
    fun `mergeMemberIntoUser rewrites settlement activity ids and transfer key`() {
        val activity = SettlementUpiTransaction(
            activityId = "act_1",
            transferKey = "guest_123|user_77|4500|2",
            payerUserId = "guest_123",
            payerName = "Guest",
            receiverUserId = "user_77",
            receiverName = "Owner",
            receiverUpiId = "owner@upi",
            amountPaise = 4_500L,
            status = SettlementUpiStatus.SUCCESS,
        )

        val mergedActivity = activity.mergeMemberIntoUser(
            fromUserId = "guest_123",
            toUserId = "user_42",
            mergedDisplayName = "Aditi",
        )

        assertEquals("user_42|user_77|4500|2", mergedActivity.transferKey)
        assertEquals("user_42", mergedActivity.payerUserId)
        assertEquals("Aditi", mergedActivity.payerName)
    }

    @Test
    fun `computeGroupBalances settles correctly after settlement activity identity merge`() {
        val expense = Expense(
            expenseId = "exp_1",
            groupId = "group_1",
            title = "Dinner",
            amountPaise = 100L,
            paidByUserId = "user_chandra",
            participantUserIds = listOf("user_chandra", "user_ankit_new"),
            splitType = SplitType.EQUAL,
            shares = emptyList(),
            createdByUserId = "user_ankit_new",
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
        )
        val staleActivity = SettlementUpiTransaction(
            activityId = "act_1",
            transferKey = "user_ankit_old|user_chandra|50|0",
            payerUserId = "user_ankit_old",
            payerName = "Ankit",
            receiverUserId = "user_chandra",
            receiverName = "Chandra",
            receiverUpiId = "chandra@upi",
            amountPaise = 50L,
            status = SettlementUpiStatus.SUCCESS,
        )

        val mergedBalances = computeGroupBalances(
            expenses = listOf(expense),
            settlementActivities = listOf(
                staleActivity.mergeMemberIntoUser(
                    fromUserId = "user_ankit_old",
                    toUserId = "user_ankit_new",
                    mergedDisplayName = "Ankit",
                ),
            ),
        )

        assertEquals(
            mapOf(
                "user_chandra" to 0L,
                "user_ankit_new" to 0L,
            ),
            mergedBalances,
        )
    }

    @Test
    fun `successfulSettlementTransactions keeps separate successful payments with same transfer key`() {
        val firstSuccess = SettlementUpiTransaction(
            activityId = "act_1",
            transferKey = "user_ankit|user_chandra|100|0",
            payerUserId = "user_ankit",
            payerName = "Ankit",
            receiverUserId = "user_chandra",
            receiverName = "Chandra",
            receiverUpiId = "chandra@upi",
            amountPaise = 100L,
            status = SettlementUpiStatus.SUCCESS,
            transactionId = "txn_1",
            handledAtEpochMs = 10L,
        )
        val secondSuccess = firstSuccess.copy(
            activityId = "act_2",
            transactionId = "txn_2",
            handledAtEpochMs = 20L,
        )

        val successfulTransactions = successfulSettlementTransactions(
            listOf(firstSuccess, secondSuccess),
        )

        assertEquals(2, successfulTransactions.size)
    }

    @Test
    fun `successfulSettlementTransactions ignores manually confirmed success`() {
        val manualSuccess = SettlementUpiTransaction(
            activityId = "act_manual",
            transferKey = "user_ankit|user_chandra|100|0",
            payerUserId = "user_ankit",
            payerName = "Ankit",
            receiverUserId = "user_chandra",
            receiverName = "Chandra",
            receiverUpiId = "chandra@upi",
            amountPaise = 100L,
            status = SettlementUpiStatus.SUCCESS,
            statusConfirmedByUser = true,
            transactionId = "txn_manual",
            handledAtEpochMs = 10L,
        )

        val successfulTransactions = successfulSettlementTransactions(listOf(manualSuccess))

        assertEquals(emptyList<SettlementUpiTransaction>(), successfulTransactions)
    }

    @Test
    fun `computeGroupBalances ignores manually confirmed success for accountability`() {
        val expense = Expense(
            expenseId = "exp_1",
            groupId = "group_1",
            title = "Dinner",
            amountPaise = 100L,
            paidByUserId = "user_chandra",
            participantUserIds = listOf("user_chandra", "user_ankit"),
            splitType = SplitType.EQUAL,
            shares = emptyList(),
            createdByUserId = "user_chandra",
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
        )
        val manualSuccess = SettlementUpiTransaction(
            activityId = "act_manual",
            transferKey = "user_ankit|user_chandra|50|0",
            payerUserId = "user_ankit",
            payerName = "Ankit",
            receiverUserId = "user_chandra",
            receiverName = "Chandra",
            receiverUpiId = "chandra@upi",
            amountPaise = 50L,
            status = SettlementUpiStatus.SUCCESS,
            statusConfirmedByUser = true,
            handledAtEpochMs = 10L,
        )

        val balances = computeGroupBalances(
            expenses = listOf(expense),
            settlementActivities = listOf(manualSuccess),
        )

        assertEquals(
            mapOf(
                "user_chandra" to 50L,
                "user_ankit" to -50L,
            ),
            balances,
        )
    }

    @Test
    fun `legacyPlaceholderAliasUserId extracts prior user id from placeholder email`() {
        val member = Member(
            userId = "user_current",
            displayName = "Ankit",
            email = "user_legacy@example.com",
            joinedAtEpochMs = 1L,
            role = Role.OWNER,
            active = true,
        )

        assertEquals(
            "user_legacy",
            legacyPlaceholderAliasUserId(
                member = member,
                activeMemberIds = setOf("user_current", "user_other"),
            ),
        )
        assertNull(
            legacyPlaceholderAliasUserId(
                member = member.copy(email = "user_current@example.com"),
                activeMemberIds = setOf("user_current", "user_other"),
            ),
        )
    }

    @Test
    fun `legacyPlaceholderAliasUserId preserves original user id casing`() {
        val member = Member(
            userId = "jPeMkZM9JBhPiPEXmeynlNSmlau1",
            displayName = "Ankit",
            email = "kwjVfqfWrbfVTl7J1GEffXoyPox2@example.com",
            joinedAtEpochMs = 1L,
            role = Role.OWNER,
            active = true,
        )

        assertEquals(
            "kwjVfqfWrbfVTl7J1GEffXoyPox2",
            legacyPlaceholderAliasUserId(
                member = member,
                activeMemberIds = setOf(
                    "jPeMkZM9JBhPiPEXmeynlNSmlau1",
                    "Tr2NNmlE3hRtBx5Edxo9yObLPzd2",
                ),
            ),
        )
    }
}
