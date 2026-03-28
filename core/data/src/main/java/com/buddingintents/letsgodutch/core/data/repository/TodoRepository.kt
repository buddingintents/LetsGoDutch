package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTasks(userId: String): Flow<List<TodoTask>>

    suspend fun addTask(userId: String, title: String): Result<TodoTask>

    suspend fun deleteTask(
        userId: String,
        taskId: String,
    ): Result<Unit>

    suspend fun updateTaskStatus(
        userId: String,
        taskId: String,
        status: TodoTaskStatus,
    ): Result<TodoTask>
}
