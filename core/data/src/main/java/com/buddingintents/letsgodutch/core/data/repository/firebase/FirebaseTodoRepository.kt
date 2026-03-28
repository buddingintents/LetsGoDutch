package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.TodoRepository
import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

class FirebaseTodoRepository(
    private val database: FirebaseDatabase,
) : TodoRepository {

    private val root = database.reference

    override fun observeTasks(userId: String): Flow<List<TodoTask>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val tasksRef = root.child("todoTasks").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children
                    .mapNotNull { it.toTodoTaskOrNull() }
                    .sortedByDescending { it.createdAtEpochMs }
                trySend(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseTodoRepo",
                    "observeTasks cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }
        tasksRef.addValueEventListener(listener)
        awaitClose { tasksRef.removeEventListener(listener) }
    }.conflate()

    override suspend fun addTask(userId: String, title: String): Result<TodoTask> {
        return runCatching {
            require(userId.isNotBlank()) { "User id is required." }
            val normalizedTitle = title.trim()
            require(normalizedTitle.isNotBlank()) { "Task title is required." }

            val now = System.currentTimeMillis()
            val taskId = root.child("todoTasks").child(userId).push().key ?: "todo_$now"
            val task = TodoTask(
                taskId = taskId,
                userId = userId,
                title = normalizedTitle,
                status = TodoTaskStatus.ACTIVE,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
            )
            root.child("todoTasks").child(userId).child(taskId).setValue(task.toFirebaseMap()).await()
            task
        }
    }

    override suspend fun updateTaskStatus(
        userId: String,
        taskId: String,
        status: TodoTaskStatus,
    ): Result<TodoTask> {
        return runCatching {
            require(userId.isNotBlank()) { "User id is required." }
            require(taskId.isNotBlank()) { "Task id is required." }

            val taskRef = root.child("todoTasks").child(userId).child(taskId)
            val existing = taskRef.get().await().toTodoTaskOrNull() ?: error("Task not found.")
            val updated = existing.copy(
                status = status,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
            taskRef.updateChildren(updated.toFirebaseMap()).await()
            updated
        }
    }

    override suspend fun deleteTask(
        userId: String,
        taskId: String,
    ): Result<Unit> {
        return runCatching {
            require(userId.isNotBlank()) { "User id is required." }
            require(taskId.isNotBlank()) { "Task id is required." }

            val taskRef = root.child("todoTasks").child(userId).child(taskId)
            if (!taskRef.get().await().exists()) {
                error("Task not found.")
            }
            taskRef.removeValue().await()
        }
    }
}

private fun DataSnapshot.toTodoTaskOrNull(): TodoTask? {
    val taskId = key ?: return null
    val userId = childString("userId")
    val title = childString("title")
    if (userId.isBlank() || title.isBlank()) return null

    val status = todoStatusFromValue(childString("status"))
    return TodoTask(
        taskId = taskId,
        userId = userId,
        title = title,
        status = status,
        createdAtEpochMs = childLong("createdAtEpochMs"),
        updatedAtEpochMs = childLong("updatedAtEpochMs"),
    )
}

private fun TodoTask.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "taskId" to taskId,
        "userId" to userId,
        "title" to title,
        "status" to status.name,
        "createdAtEpochMs" to createdAtEpochMs,
        "updatedAtEpochMs" to updatedAtEpochMs,
    )
}

private fun todoStatusFromValue(value: String): TodoTaskStatus {
    return TodoTaskStatus.entries.firstOrNull { it.name == value } ?: TodoTaskStatus.ACTIVE
}
