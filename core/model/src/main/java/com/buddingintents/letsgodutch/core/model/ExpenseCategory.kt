package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseCategory(
    val displayLabel: String,
) {
    FOOD("Food"),
    TRAVEL("Travel"),
    STAY("Stay"),
    FUN("Fun"),
    OTHER("Other"),
}
