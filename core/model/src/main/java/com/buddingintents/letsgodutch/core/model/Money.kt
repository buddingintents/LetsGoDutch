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

    fun toInrDisplay(): String = toRupeeDisplay()

    fun toRupeeDisplay(): String {
        return formatIndianCurrency(paise)
    }

    companion object {
        val Zero = Money(0)
    }
}

fun formatIndianCurrency(
    paise: Long,
    currencyPrefix: String = "\u20B9",
): String {
    val absValue = abs(paise)
    val rupees = absValue / 100
    val remainder = absValue % 100
    val sign = if (paise < 0) "-" else ""
    return "$sign$currencyPrefix${formatIndianNumber(rupees)}.${remainder.toString().padStart(2, '0')}"
}

private fun formatIndianNumber(value: Long): String {
    if (value < 1000) return value.toString()

    val digits = value.toString()
    val lastThreeDigits = digits.takeLast(3)
    var leadingDigits = digits.dropLast(3)
    val groups = mutableListOf<String>()

    while (leadingDigits.length > 2) {
        groups += leadingDigits.takeLast(2)
        leadingDigits = leadingDigits.dropLast(2)
    }
    if (leadingDigits.isNotEmpty()) {
        groups += leadingDigits
    }

    return groups.asReversed().joinToString(",") + "," + lastThreeDigits
}
