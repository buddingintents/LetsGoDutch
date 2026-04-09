package com.buddingintents.letsgodutch.feature.groups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.buddingintents.letsgodutch.core.designsystem.component.AvatarBadge
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.SettlementState
import com.buddingintents.letsgodutch.core.model.SettlementSummary
import com.buddingintents.letsgodutch.core.model.UnsettledGroupsSummary
import com.buddingintents.letsgodutch.core.model.summarizeGroupNetBalances
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

@Composable
fun GroupsListScreen(
    groups: List<Group>,
    currentUserDisplayName: String,
    currentUserId: String,
    groupNetPaiseById: Map<String, Long>,
    appIconResId: Int? = null,
    onOpenGroup: (groupId: String) -> Unit,
    onShareGroupInvite: (group: Group) -> Unit,
    onCopyGroupInvite: (group: Group) -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupsSummary = remember(groupNetPaiseById) {
        summarizeGroupNetBalances(groupNetPaiseById)
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        GroupsBackdropArt(modifier = Modifier.matchParentSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GroupsHeroCard(
                    displayName = currentUserDisplayName,
                    totalGroups = groups.size,
                    groupsSummary = groupsSummary,
                    appIconResId = appIconResId,
                    onCreateGroupClick = onCreateGroupClick,
                    onJoinGroupClick = onJoinGroupClick,
                )
            }

            if (groups.isEmpty()) {
                item {
                    EmptyGroupsHint()
                }
            } else {
                items(groups, key = { it.groupId }) { group ->
                    GroupCard(
                        group = group,
                        isOwnedByCurrentUser = group.ownerUserId == currentUserId,
                        settlementSummary = SettlementSummary(groupNetPaiseById[group.groupId] ?: 0L),
                        onOpenGroup = { onOpenGroup(group.groupId) },
                        onShareGroupInvite = { onShareGroupInvite(group) },
                        onCopyInvite = { onCopyGroupInvite(group) },
                    )
                }
            }

            item {
                GroupsBannerAdContainer()
            }
        }
    }
}

@Composable
private fun GroupsHeroCard(
    displayName: String,
    totalGroups: Int,
    groupsSummary: UnsettledGroupsSummary,
    appIconResId: Int?,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
) {
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    val welcomeName = displayName.toHeroDisplayName()
    val settlement = groupsSummary.settlement
    val netValueColor = when (settlement.state) {
        SettlementState.RECEIVABLE -> MintGreen
        SettlementState.PAYABLE -> Color(0xFFFFB4AB)
        SettlementState.SETTLED -> Color.White
    }
    val netLabelColor = when (settlement.state) {
        SettlementState.RECEIVABLE -> MintGreen.copy(alpha = 0.82f)
        SettlementState.PAYABLE -> Color(0xFFFFB4AB).copy(alpha = 0.9f)
        SettlementState.SETTLED -> Color.White.copy(alpha = 0.72f)
    }
    val supportingText = when {
        totalGroups == 0 -> "Create a group or join one with an invite code to get started."
        groupsSummary.unsettledGroupCount == 0 -> "All your groups are settled right now."
        groupsSummary.hasMixedBalances ->
            "${groupsSummary.unsettledGroupCount} groups need settlement across both sides."
        else -> "${groupsSummary.unsettledGroupCount} groups need settlement right now."
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(MintTeal, NightSoft, Charcoal),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeroAppBadge(appIconResId = appIconResId)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Group Summary",
                        style = MaterialTheme.typography.labelMedium,
                        color = MintGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Hi $welcomeName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                }
                Surface(
                    modifier = Modifier.clickable { showInfoDialog = true },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "i",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.76f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeroMetricCard(
                    label = "Groups",
                    value = totalGroups.toString(),
                    modifier = Modifier.weight(0.9f),
                )
                HeroMetricCard(
                    label = settlement.label,
                    value = settlement.amountDisplay,
                    valueColor = netValueColor,
                    labelColor = netLabelColor,
                    modifier = Modifier.weight(1.5f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GradientButton(
                    text = "Create Group",
                    onClick = onCreateGroupClick,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = onJoinGroupClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                ) {
                    Text(
                        text = "Join Group",
                        color = Color.White,
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Groups overview") },
            text = {
                Text(
                    "See your overall settlement position, open a group, and share its invite from here. " +
                        "The group cards also show whether you owe or should receive money.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Close")
                }
            },
        )
    }
}

@Composable
private fun HeroMetricCard(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    labelColor: Color = Color.White.copy(alpha = 0.72f),
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeroAppBadge(
    appIconResId: Int?,
) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (appIconResId != null) {
                Image(
                    painter = painterResource(id = appIconResId),
                    contentDescription = "Let's Go Dutch app icon",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp)),
                )
            } else {
                Text(
                    text = "LGD",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EmptyGroupsHint(
) {
    Surface(
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No groups created yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Use the Create Group or Join Group buttons above to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    isOwnedByCurrentUser: Boolean,
    settlementSummary: SettlementSummary,
    onOpenGroup: () -> Unit,
    onShareGroupInvite: () -> Unit,
    onCopyInvite: () -> Unit,
) {
    val settlementContainerColor = when (settlementSummary.state) {
        SettlementState.RECEIVABLE -> MintGreen.copy(alpha = 0.14f)
        SettlementState.PAYABLE -> MaterialTheme.colorScheme.errorContainer
        SettlementState.SETTLED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    }
    val settlementContentColor = when (settlementSummary.state) {
        SettlementState.RECEIVABLE -> MintTeal
        SettlementState.PAYABLE -> MaterialTheme.colorScheme.onErrorContainer
        SettlementState.SETTLED -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenGroup),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarBadge(label = group.name, size = 44.dp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isOwnedByCurrentUser) {
                            InviteMetaPill(
                                text = "Owner",
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    if (group.description.isNotBlank()) {
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactMetaPill(
                    label = "Invite code",
                    value = group.inviteCode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    actionText = "Copy",
                    onClick = onCopyInvite,
                )
                CompactMetaPill(
                    label = settlementSummary.label,
                    value = settlementSummary.amountDisplay,
                    containerColor = settlementContainerColor,
                    contentColor = settlementContentColor,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = group.inviteMetaSummary(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(onClick = onShareGroupInvite) {
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun InviteMetaPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun CompactMetaPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!actionText.isNullOrBlank()) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupsBackdropArt(
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.background
    val palette = listOf(
        MintGreen.copy(alpha = 0.18f),
        MintTeal.copy(alpha = 0.16f),
        primary.copy(alpha = 0.14f),
        secondary.copy(alpha = 0.12f),
        tertiary.copy(alpha = 0.12f),
    )
    val orbSpecs = remember(primary, secondary, tertiary, surface) {
        val random = Random(System.currentTimeMillis())
        List(6) { index ->
            AmbientOrbSpec(
                alignment = ambientAlignments[index % ambientAlignments.size],
                sizeDp = random.nextInt(150, 280),
                offsetXDp = random.nextInt(-90, 90),
                offsetYDp = random.nextInt(-140, 140),
                color = palette[index % palette.size],
            )
        }
    }
    val ribbonSpecs = remember(primary, secondary, tertiary, surface) {
        val random = Random(System.currentTimeMillis() + 91L)
        List(3) { index ->
            AmbientRibbonSpec(
                widthDp = random.nextInt(180, 320),
                heightDp = random.nextInt(54, 92),
                offsetXDp = random.nextInt(-120, 120),
                offsetYDp = random.nextInt(-260, 260),
                rotationZ = random.nextInt(-48, 48).toFloat(),
                colors = listOf(
                    palette[index % palette.size],
                    palette[(index + 2) % palette.size].copy(alpha = 0.08f),
                ),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface),
    ) {
        orbSpecs.forEach { spec ->
            Box(
                modifier = Modifier
                    .align(spec.alignment)
                    .offset(x = spec.offsetXDp.dp, y = spec.offsetYDp.dp)
                    .size(spec.sizeDp.dp)
                    .clip(CircleShape)
                    .background(spec.color)
                    .blur(120.dp),
            )
        }

        ribbonSpecs.forEach { spec ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = spec.offsetXDp.dp, y = spec.offsetYDp.dp)
                    .width(spec.widthDp.dp)
                    .height(spec.heightDp.dp)
                    .graphicsLayer { rotationZ = spec.rotationZ }
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(spec.colors))
                    .blur(138.dp),
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(surface.copy(alpha = 0.80f)),
        )
    }
}

@Composable
private fun GroupsBannerAdContainer() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MintGlow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel(text = "Sponsored")
            GroupsBannerAd(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GroupsBannerAd(
    modifier: Modifier = Modifier,
) {
    val adWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    key(adWidthDp) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    adUnitId = GROUPS_BANNER_AD_UNIT_ID
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            viewContext,
                            adWidthDp,
                        ),
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}

private data class AmbientOrbSpec(
    val alignment: Alignment,
    val sizeDp: Int,
    val offsetXDp: Int,
    val offsetYDp: Int,
    val color: Color,
)

private data class AmbientRibbonSpec(
    val widthDp: Int,
    val heightDp: Int,
    val offsetXDp: Int,
    val offsetYDp: Int,
    val rotationZ: Float,
    val colors: List<Color>,
)

private val ambientAlignments: List<Alignment> = listOf(
    Alignment.TopStart,
    Alignment.TopEnd,
    Alignment.CenterStart,
    Alignment.CenterEnd,
    Alignment.BottomStart,
    Alignment.BottomEnd,
)

private const val GROUPS_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/8701762571"
private val inviteDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

private fun String.toHeroDisplayName(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return "there"
    return trimmed.replaceFirstChar { first ->
        if (first.isLowerCase()) {
            first.titlecase(Locale.getDefault())
        } else {
            first.toString()
        }
    }
}

private fun Group.inviteMetaSummary(now: Long = System.currentTimeMillis()): String {
    return when {
        autoRenewInvite -> "Invite auto-renews and stays ready to share."
        inviteExpiryEpochMs <= now -> "Invite expired on ${inviteExpiryEpochMs.toInviteDateLabel()}."
        else -> "Invite active until ${inviteExpiryEpochMs.toInviteDateLabel()}."
    }
}

private fun Long.toInviteDateLabel(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(inviteDateFormatter)
}
