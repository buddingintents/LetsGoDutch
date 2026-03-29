package com.buddingintents.letsgodutch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    currentDisplayName: String,
    isSavingDisplayName: Boolean,
    appUpdateSummary: String,
    isCheckingForAppUpdate: Boolean,
    isDownloadedUpdateReady: Boolean,
    onUpdateDisplayName: (String) -> Unit,
    onResetTourClick: () -> Unit,
    onCheckForAppUpdateClick: () -> Unit,
    onInstallDownloadedUpdateClick: () -> Unit,
    onOpenPlayStoreUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayName by rememberSaveable(currentDisplayName) { mutableStateOf(currentDisplayName) }
    val normalizedInitialName = currentDisplayName.trim()
    val normalizedEditedName = displayName.trim()
    val canSaveName = normalizedEditedName.isNotBlank() &&
        normalizedEditedName != normalizedInitialName &&
        !isSavingDisplayName

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Your Name",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Update the name shown for your account and sync it across the groups you are part of.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onUpdateDisplayName(normalizedEditedName) },
            enabled = canSaveName,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSavingDisplayName) "Saving..." else "Save Name")
        }

        Text(
            text = "App Tour",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Reset the onboarding tour so it appears again on your next screen open.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onResetTourClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reset App Tour")
        }

        Text(
            text = "App Updates",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = appUpdateSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onCheckForAppUpdateClick,
            enabled = !isCheckingForAppUpdate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isCheckingForAppUpdate) "Checking..." else "Check for Updates")
        }
        if (isDownloadedUpdateReady) {
            Button(
                onClick = onInstallDownloadedUpdateClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Install Downloaded Update")
            }
        }
        Button(
            onClick = onOpenPlayStoreUpdateClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open in Google Play")
        }
    }
}
