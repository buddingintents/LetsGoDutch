package com.buddingintents.letsgodutch.core.model

data class TodoTask(
    val taskId: String,
    val userId: String,
    val title: String,
    val status: TodoTaskStatus = TodoTaskStatus.ACTIVE,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
