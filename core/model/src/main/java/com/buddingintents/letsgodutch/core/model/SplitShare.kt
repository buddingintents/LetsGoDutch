package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SplitShare(
    val userId: String,
    val amountPaise: Long? = null,
    val percentage: Double? = null,
    val customUnits: Double? = null,
)
