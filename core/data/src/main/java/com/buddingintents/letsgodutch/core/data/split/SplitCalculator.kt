package com.buddingintents.letsgodutch.core.data.split

import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import kotlin.math.floor

object SplitCalculator {

    fun allocate(
        totalPaise: Long,
        participantUserIds: List<String>,
        splitType: SplitType,
        shares: List<SplitShare>,
    ): Result<Map<String, Long>> {
        if (totalPaise <= 0L) {
            return Result.failure(IllegalArgumentException("Total amount must be positive."))
        }
        if (participantUserIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("At least one participant is required."))
        }

        val participantSet = participantUserIds.toSet()
        if (participantSet.size != participantUserIds.size) {
            return Result.failure(IllegalArgumentException("Duplicate participant ids are not allowed."))
        }

        val result = when (splitType) {
            SplitType.EQUAL -> allocateEqual(totalPaise, participantUserIds)
            SplitType.EXACT -> allocateExact(totalPaise, participantSet, shares)
            SplitType.PERCENTAGE -> allocatePercentage(totalPaise, participantSet, shares)
            SplitType.CUSTOM -> allocateCustom(totalPaise, participantSet, shares)
        }

        return result.mapCatching { allocation ->
            val sum = allocation.values.sum()
            require(sum == totalPaise) {
                "Allocation mismatch. Expected $totalPaise, got $sum."
            }
            allocation
        }
    }

    private fun allocateEqual(totalPaise: Long, participants: List<String>): Result<Map<String, Long>> {
        val base = totalPaise / participants.size
        var remainder = totalPaise % participants.size

        val map = linkedMapOf<String, Long>()
        participants.forEach { userId ->
            val extra = if (remainder > 0) 1L else 0L
            map[userId] = base + extra
            if (remainder > 0) {
                remainder -= 1
            }
        }
        return Result.success(map)
    }

    private fun allocateExact(
        totalPaise: Long,
        participants: Set<String>,
        shares: List<SplitShare>,
    ): Result<Map<String, Long>> {
        if (shares.size != participants.size) {
            return Result.failure(IllegalArgumentException("Exact split requires one amount per participant."))
        }

        val map = mutableMapOf<String, Long>()
        var sum = 0L
        shares.forEach { share ->
            if (share.userId !in participants) {
                return Result.failure(IllegalArgumentException("Unknown participant ${share.userId}."))
            }
            val amount = share.amountPaise
                ?: return Result.failure(IllegalArgumentException("Exact split requires amount per participant."))
            if (amount < 0L) {
                return Result.failure(IllegalArgumentException("Negative split amount for ${share.userId}."))
            }
            map[share.userId] = amount
            sum += amount
        }
        if (sum != totalPaise) {
            return Result.failure(IllegalArgumentException("Exact shares must sum to total amount."))
        }
        return Result.success(map)
    }

    private fun allocatePercentage(
        totalPaise: Long,
        participants: Set<String>,
        shares: List<SplitShare>,
    ): Result<Map<String, Long>> {
        if (shares.size != participants.size) {
            return Result.failure(IllegalArgumentException("Percentage split requires percentage for all participants."))
        }

        val raw = mutableMapOf<String, Double>()
        var percentageTotal = 0.0
        shares.forEach { share ->
            if (share.userId !in participants) {
                return Result.failure(IllegalArgumentException("Unknown participant ${share.userId}."))
            }
            val percent = share.percentage
                ?: return Result.failure(IllegalArgumentException("Missing percentage for ${share.userId}."))
            if (percent < 0.0) {
                return Result.failure(IllegalArgumentException("Negative percentage for ${share.userId}."))
            }
            raw[share.userId] = percent
            percentageTotal += percent
        }

        if (kotlin.math.abs(percentageTotal - 100.0) > 0.001) {
            return Result.failure(IllegalArgumentException("Percentage split must sum to 100."))
        }

        return proportionalAllocate(
            totalPaise = totalPaise,
            factors = raw,
        )
    }

    private fun allocateCustom(
        totalPaise: Long,
        participants: Set<String>,
        shares: List<SplitShare>,
    ): Result<Map<String, Long>> {
        if (shares.size != participants.size) {
            return Result.failure(IllegalArgumentException("Custom split requires a weight for all participants."))
        }

        val factors = mutableMapOf<String, Double>()
        shares.forEach { share ->
            if (share.userId !in participants) {
                return Result.failure(IllegalArgumentException("Unknown participant ${share.userId}."))
            }
            val units = share.customUnits
                ?: return Result.failure(IllegalArgumentException("Missing custom unit for ${share.userId}."))
            if (units < 0.0) {
                return Result.failure(IllegalArgumentException("Negative custom unit for ${share.userId}."))
            }
            factors[share.userId] = units
        }
        if (factors.values.sum() <= 0.0) {
            return Result.failure(IllegalArgumentException("Custom unit total must be greater than zero."))
        }
        return proportionalAllocate(totalPaise, factors)
    }

    private fun proportionalAllocate(
        totalPaise: Long,
        factors: Map<String, Double>,
    ): Result<Map<String, Long>> {
        val factorSum = factors.values.sum()
        if (factorSum <= 0.0) {
            return Result.failure(IllegalArgumentException("Factors must be positive."))
        }

        data class Fractional(val userId: String, val floorValue: Long, val remainder: Double)

        var used = 0L
        val fractional = factors.map { (userId, value) ->
            val raw = (totalPaise.toDouble() * value) / factorSum
            val base = floor(raw).toLong()
            used += base
            Fractional(userId, base, raw - base)
        }.toMutableList()

        var remaining = totalPaise - used
        val sorted = fractional.sortedByDescending { it.remainder }
        val allocation = sorted.associate { it.userId to it.floorValue }.toMutableMap()

        var index = 0
        while (remaining > 0) {
            val userId = sorted[index % sorted.size].userId
            allocation[userId] = (allocation[userId] ?: 0L) + 1L
            remaining -= 1L
            index += 1
        }
        return Result.success(allocation)
    }
}
