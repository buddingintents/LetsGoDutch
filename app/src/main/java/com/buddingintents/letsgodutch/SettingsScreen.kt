package com.buddingintents.letsgodutch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.designsystem.component.AvatarBadge
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark

@Composable
fun SettingsScreen(
    currentDisplayName: String,
    currentAccountId: String,
    currentAccountSummary: String,
    currentAccountEmail: String,
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
    val displayNameSeed = currentDisplayName.trim().ifBlank { "Member" }
    var displayName by rememberSaveable(displayNameSeed) { mutableStateOf(displayNameSeed) }
    val normalizedInitialName = displayNameSeed.trim()
    val normalizedEditedName = displayName.trim()
    val canSaveName = normalizedEditedName.isNotBlank() &&
        normalizedEditedName != normalizedInitialName &&
        !isSavingDisplayName

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingsProfileCard(
                    displayName = displayName.ifBlank { displayNameSeed },
                    accountSummary = currentAccountSummary,
                    accountEmail = currentAccountEmail,
                    accountId = currentAccountId.ifBlank { "Unavailable" },
                    editableDisplayName = displayName,
                    onDisplayNameChange = { displayName = it },
                )
            }
            item {
                GradientButton(
                    text = if (isSavingDisplayName) "Saving..." else "Save Changes",
                    onClick = { onUpdateDisplayName(normalizedEditedName) },
                    enabled = canSaveName,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SettingsSurfaceCard {
                    SectionLabel(text = "About")
                    Text(
                        text = "Updates & Store",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = appUpdateSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SettingsActionRow(
                        icon = Icons.Default.SettingsSuggest,
                        title = if (isCheckingForAppUpdate) "Checking for updates..." else "Check for updates",
                        supportingText = "Run an on-demand app update check from Google Play.",
                        actionLabel = if (isCheckingForAppUpdate) "Checking" else "Check",
                        onClick = onCheckForAppUpdateClick,
                        enabled = !isCheckingForAppUpdate,
                    )
                    if (isDownloadedUpdateReady) {
                        GradientButton(
                            text = "Install Downloaded Update",
                            onClick = onInstallDownloadedUpdateClick,
                            modifier = Modifier.fillMaxWidth(),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                    SettingsActionRow(
                        icon = Icons.Default.StarRate,
                        title = "Open in Google Play",
                        supportingText = "View the store listing and install the latest public release there.",
                        actionLabel = "Open",
                        onClick = onOpenPlayStoreUpdateClick,
                    )
                }
            }
            item {
                SettingsSurfaceCard {
                    SectionLabel(text = "Guidance")
                    Text(
                        text = "App Tour",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Bring the onboarding overlay back if you want to revisit the core flows and shortcuts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SettingsActionRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Reset app tour",
                        supportingText = "Show the guided walkthrough the next time you open the main screens.",
                        actionLabel = "Reset",
                        onClick = onResetTourClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsProfileCard(
    displayName: String,
    accountSummary: String,
    accountEmail: String,
    accountId: String,
    editableDisplayName: String,
    onDisplayNameChange: (String) -> Unit,
) {
    val primarySupportingText = accountEmail.ifBlank { accountSummary }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(MintTeal, NightSoft, Charcoal),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = MintGlow,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarBadge(
                            label = displayName,
                            size = 56.dp,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Let's GO Dutch User",
                                style = MaterialTheme.typography.labelMedium,
                                color = MintGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = primarySupportingText.ifBlank { "Stable account identity" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnDark.copy(alpha = 0.76f),
                            )
                            if (accountEmail.isNotBlank() && accountSummary.isNotBlank()) {
                                Text(
                                    text = accountSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MintGreen,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.86f),
                    )
                }
                OutlinedTextField(
                    value = editableDisplayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedLabelColor = MintGreen,
                        unfocusedLabelColor = TextOnDark.copy(alpha = 0.65f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MintGreen,
                    ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Stable account ID",
                            style = MaterialTheme.typography.labelMedium,
                            color = MintGreen,
                        )
                        Text(
                            text = accountId,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "Preserved across upgrades",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDark.copy(alpha = 0.68f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSurfaceCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    RoundedCornerShape(22.dp),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    supportingText: String,
    actionLabel: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 1f else 0.72f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (enabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
