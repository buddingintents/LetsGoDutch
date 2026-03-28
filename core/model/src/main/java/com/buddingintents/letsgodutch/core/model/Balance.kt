package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Balance(
    val userId: String,
    val netPaise: Long,
) {
    val owes: Boolean get() = netPaise < 0
    val getsBack: Boolean get() = netPaise > 0
}
