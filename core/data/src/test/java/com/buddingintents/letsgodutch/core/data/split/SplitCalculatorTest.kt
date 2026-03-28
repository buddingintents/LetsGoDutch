package com.buddingintents.letsgodutch.core.data.split

import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun equalSplit_dividesAndDistributesRemainder() {
        val result = SplitCalculator.allocate(
            totalPaise = 1001,
            participantUserIds = listOf("u1", "u2", "u3"),
            splitType = SplitType.EQUAL,
            shares = emptyList(),
        ).getOrThrow()

        assertEquals(1001, result.values.sum())
        assertEquals(334L, result["u1"])
        assertEquals(334L, result["u2"])
        assertEquals(333L, result["u3"])
    }

    @Test
    fun percentageSplit_allocatesAllPaise() {
        val result = SplitCalculator.allocate(
            totalPaise = 999,
            participantUserIds = listOf("u1", "u2"),
            splitType = SplitType.PERCENTAGE,
            shares = listOf(
                SplitShare(userId = "u1", percentage = 75.0),
                SplitShare(userId = "u2", percentage = 25.0),
            ),
        ).getOrThrow()

        assertEquals(999, result.values.sum())
        assertTrue((result["u1"] ?: 0L) > (result["u2"] ?: 0L))
    }

    @Test
    fun exactSplit_failsWhenTotalMismatch() {
        val result = SplitCalculator.allocate(
            totalPaise = 500,
            participantUserIds = listOf("u1", "u2"),
            splitType = SplitType.EXACT,
            shares = listOf(
                SplitShare(userId = "u1", amountPaise = 300),
                SplitShare(userId = "u2", amountPaise = 100),
            ),
        )

        assertTrue(result.isFailure)
    }
}
