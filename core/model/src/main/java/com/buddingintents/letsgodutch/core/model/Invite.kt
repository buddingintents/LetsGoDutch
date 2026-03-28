package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Invite(
    val groupId: String,
    val inviteCode: String,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long,
    val reusable: Boolean = true,
)
