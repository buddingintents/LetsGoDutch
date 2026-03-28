package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val createdAtEpochMs: Long,
    val isAnonymous: Boolean = false,
    val identifier: String = "",
    val deviceId: String = "",
    val deviceModel: String = "",
    val country: String = "",
)
