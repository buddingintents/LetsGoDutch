package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
@JvmInline
value class Money(val paise: Long) {
    operator fun plus(other: Money): Money = Money(paise + other.paise)
    operator fun minus(other: Money): Money = Money(paise - other.paise)
    fun isPositive(): Boolean = paise > 0
    fun isNegative(): Boolean = paise < 0

    fun toInrDisplay(): String {
        val absValue = abs(paise)
        val rupees = absValue / 100
        val remainder = absValue % 100
        val prefix = if (paise < 0) "-₹" else "₹"
        return "$prefix$rupees.${remainder.toString().padStart(2, '0')}"
    }

    fun toRupeeDisplay(): String {
        val absValue = abs(paise)
        val rupees = absValue / 100
        val remainder = absValue % 100
        val prefix = if (paise < 0) "-\u20B9" else "\u20B9"
        return "$prefix$rupees.${remainder.toString().padStart(2, '0')}"
    }

    companion object {
        val Zero = Money(0)
    }
}
