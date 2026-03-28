package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: String,
    val name: String,
    val ownerUserId: String,
    val currencyCode: String = "INR",
    val maxMembers: Int = 50,
    val createdAtEpochMs: Long,
    val description: String = "",
    val inviteCode: String,
    val inviteExpiryEpochMs: Long,
    val autoRenewInvite: Boolean = true,
    val selectAllMembersByDefaultForExpenses: Boolean = false,
    val active: Boolean = true,
)
