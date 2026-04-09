package com.buddingintents.letsgodutch.feature.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.model.Group

@Deprecated("Use GroupsListScreen as the canonical groups landing surface during the revamp.")
@Composable
fun GroupsScreen(
    groups: List<Group>,
    onOpenGroup: (groupId: String) -> Unit,
    onShareGroupInvite: (group: Group) -> Unit,
    onCreateGroup: (name: String) -> Unit,
    onJoinGroup: (inviteCode: String) -> Unit,
    message: String,
    modifier: Modifier = Modifier,
) {
    var groupName by rememberSaveable { mutableStateOf("") }
    var inviteCode by rememberSaveable { mutableStateOf("") }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Your Groups",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("New group name") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onCreateGroup(groupName.trim())
                groupName = ""
            },
            enabled = groupName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Group")
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it.uppercase() },
            label = { Text("Join with invite code") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onJoinGroup(inviteCode.trim())
                inviteCode = ""
            },
            enabled = inviteCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Join Group")
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(groups, key = { it.groupId }) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenGroup(group.groupId) },
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Invite: ${group.inviteCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(onClick = { onShareGroupInvite(group) }) {
                            Text("Share Link")
                        }
                    }
                }
            }
        }
    }
}
