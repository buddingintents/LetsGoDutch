package com.buddingintents.letsgodutch.core.model

data class JoinGroupPreview(
    val group: Group,
    val alreadyJoined: Boolean,
    val claimableMembers: List<Member>,
)
