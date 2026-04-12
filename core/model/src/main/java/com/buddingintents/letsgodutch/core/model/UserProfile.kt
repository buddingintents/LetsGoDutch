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
    val primaryAuthProvider: String = "",
    val linkedProviders: List<String> = emptyList(),
    val upgradedFromAnonymousAtEpochMs: Long? = null,
    val publicAccountId: String = "",
    val upiId: String = "",
) {
    val displayId: String
        get() = publicAccountId.trim().ifBlank { userId.take(8).uppercase() }
}
