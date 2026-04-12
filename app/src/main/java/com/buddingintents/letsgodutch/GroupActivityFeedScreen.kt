package com.buddingintents.letsgodutch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.model.GroupActivity
import com.buddingintents.letsgodutch.core.model.GroupActivityType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GroupActivityFeedScreen(
    activities: List<GroupActivity>,
    modifier: Modifier = Modifier,
) {
    if (activities.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Group actions will appear here once members start using this group.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = activities,
            key = { activity -> activity.activityId.ifBlank { "${activity.type.name}_${activity.createdAtEpochMs}" } },
        ) { activity ->
            val accent = activityAccentColor(activity.type)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = accent.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
                        ) {
                            Text(
                                text = activity.type.displayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                        Text(
                            text = activity.createdAtEpochMs.toRelativeActivityTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    if (activity.detail.isNotBlank()) {
                        Text(
                            text = activity.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun activityAccentColor(type: GroupActivityType): Color {
    return when (type) {
        GroupActivityType.GROUP_CREATED,
        GroupActivityType.MEMBER_JOINED,
        GroupActivityType.MEMBER_ADDED,
        -> MaterialTheme.colorScheme.primary
        GroupActivityType.SETTLEMENT_COMPLETED -> MaterialTheme.colorScheme.tertiary
        GroupActivityType.EXPENSE_ADDED,
        GroupActivityType.EXPENSE_UPDATED,
        -> MaterialTheme.colorScheme.secondary
        GroupActivityType.MEMBER_REMOVED,
        GroupActivityType.EXPENSE_DELETED,
        -> MaterialTheme.colorScheme.error
    }
}

private fun Long.toRelativeActivityTime(): String {
    val now = System.currentTimeMillis()
    val delta = (now - this).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        delta < minute -> "just now"
        delta < hour -> "${delta / minute}m ago"
        delta < day -> "${delta / hour}h ago"
        delta < 7 * day -> "${delta / day}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.US).format(Date(this))
    }
}
