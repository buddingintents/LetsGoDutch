package com.buddingintents.letsgodutch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTasksScreen(
    tasks: List<TodoTask>,
    onMarkCompleted: (TodoTask) -> Unit,
    onCancelTask: (TodoTask) -> Unit,
    onDeleteTask: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTasks = tasks
        .filter { it.status == TodoTaskStatus.ACTIVE }
        .sortedByDescending { it.updatedAtEpochMs }
    val completedTasks = tasks
        .filter { it.status == TodoTaskStatus.COMPLETED }
        .sortedByDescending { it.updatedAtEpochMs }
    val canceledTasks = tasks
        .filter { it.status == TodoTaskStatus.CANCELED }
        .sortedByDescending { it.updatedAtEpochMs }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        Text(
            text = "Swipe right to complete, swipe left to cancel, or tap delete.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No tasks yet. Tap Add Task to create your first one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (activeTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active (${activeTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                items(activeTasks, key = { it.taskId }) { task ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onMarkCompleted(task)
                                    true
                                }

                                SwipeToDismissBoxValue.EndToStart -> {
                                    onCancelTask(task)
                                    true
                                }

                                SwipeToDismissBoxValue.Settled -> false
                            }
                        },
                    )

                    val dismissDirection = dismissState.dismissDirection
                    val backgroundColor = when (dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val backgroundLabel = when (dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> "Mark Completed"
                        SwipeToDismissBoxValue.EndToStart -> "Cancel Task"
                        SwipeToDismissBoxValue.Settled -> "Swipe"
                    }
                    val contentAlignment = when (dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                contentAlignment = contentAlignment,
                            ) {
                                Text(
                                    text = backgroundLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        },
                    ) {
                        TaskCard(
                            task = task,
                            onDeleteClick = { onDeleteTask(task) },
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed (${completedTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(completedTasks.take(10), key = { it.taskId }) { task ->
                        TaskStatusCard(
                            label = task.title,
                            statusText = "Completed ${task.updatedAtEpochMs.toReadableDateTime()}",
                            onDeleteClick = { onDeleteTask(task) },
                        )
                    }
                }

                if (canceledTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Canceled (${canceledTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(canceledTasks.take(10), key = { it.taskId }) { task ->
                        TaskStatusCard(
                            label = task.title,
                            statusText = "Canceled ${task.updatedAtEpochMs.toReadableDateTime()}",
                            onDeleteClick = { onDeleteTask(task) },
                        )
                    }
                }
            }
        }

        LetsGoDutchBannerAd(
            productionAdUnitId = TODO_BANNER_AD_UNIT_ID,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun TaskCard(
    task: TodoTask,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Updated ${task.updatedAtEpochMs.toReadableDateTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                )
            }
        }
    }
}

@Composable
private fun TaskStatusCard(
    label: String,
    statusText: String,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                )
            }
        }
    }
}

@Composable
fun AddTodoTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add To-Do Task") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAddTask(title.trim()) },
                enabled = title.trim().isNotBlank(),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private val todoDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.US)

private const val TODO_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/5556724481"

private fun Long.toReadableDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(todoDateFormatter)
}
