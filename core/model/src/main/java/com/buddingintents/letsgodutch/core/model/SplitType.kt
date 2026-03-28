package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE,
    CUSTOM,
}
