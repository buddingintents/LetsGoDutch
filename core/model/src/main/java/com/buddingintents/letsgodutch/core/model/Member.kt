package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val userId: String,
    val displayName: String,
    val email: String,
    val identifier: String = "",
    val photoUrl: String? = null,
    val joinedAtEpochMs: Long,
    val role: Role = Role.MEMBER,
    val active: Boolean = true,
    val upiId: String = "",
)
