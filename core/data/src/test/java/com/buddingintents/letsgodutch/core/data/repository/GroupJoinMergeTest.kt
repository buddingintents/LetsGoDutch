package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import org.junit.Assert.assertEquals
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
}
