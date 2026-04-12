package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class GroupActivityType(
    val displayLabel: String,
) {
    GROUP_CREATED("Created"),
    MEMBER_JOINED("Joined"),
    MEMBER_ADDED("Member"),
    MEMBER_REMOVED("Removed"),
    EXPENSE_ADDED("Expense"),
    EXPENSE_UPDATED("Updated"),
    EXPENSE_DELETED("Deleted"),
    SETTLEMENT_COMPLETED("Settled"),
}

@Serializable
data class GroupActivity(
    val activityId: String = "",
    val groupId: String,
    val type: GroupActivityType,
    val actorUserId: String = "",
    val actorName: String = "",
    val title: String,
    val detail: String = "",
    val createdAtEpochMs: Long,
)
