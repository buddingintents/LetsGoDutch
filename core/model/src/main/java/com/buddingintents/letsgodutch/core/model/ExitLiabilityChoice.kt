package com.buddingintents.letsgodutch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExitLiabilityChoice {
    DISTRIBUTE_EQUAL_TO_ACTIVE_MEMBERS,
    ABSORB_BY_OWNER,
}
