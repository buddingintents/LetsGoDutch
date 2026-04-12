package com.buddingintents.letsgodutch.feature.settlement

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.designsystem.component.AvatarBadge
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.model.SettlementUpiStatus
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.CoralSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.Night
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark

data class SettlementPreviewSummaryUi(
    val groupName: String,
    val activeMembersCount: Int,
    val expenseEntriesCount: Int,
    val totalAmountDisplay: String,
    val openBalancesCount: Int,
)

data class SettlementTransferUi(
    val transferKey: String,
    val payerUserId: String,
    val receiverUserId: String,
    val payerName: String,
    val receiverName: String,
    val amountDisplay: String,
    val amountPaise: Long = 0L,
    val receiverUpiId: String = "",
    val canPayViaUpi: Boolean = false,
)

data class SettlementTrackedUpiResponseUi(
    val payerName: String,
    val receiverName: String,
    val amountDisplay: String,
    val status: SettlementUpiStatus,
    val handledAtDisplay: String,
    val referenceDisplay: String = "",
)

data class SettlementSuccessUi(
    val groupName: String,
    val transferCount: Int,
    val totalAmountDisplay: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementPreviewScreen(
    summary: SettlementPreviewSummaryUi,
    transfers: List<SettlementTransferUi>,
    trackedUpiResponses: List<SettlementTrackedUpiResponseUi>,
    isOwner: Boolean,
    hasExpenses: Boolean,
    isFinalizing: Boolean,
    successState: SettlementSuccessUi?,
    isSuccessVisible: Boolean,
    onConfirm: () -> Unit,
    onTransferUpiPayClick: (SettlementTransferUi) -> Unit,
    modifier: Modifier = Modifier,
    navigationAction: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Settlement Preview") },
                    navigationIcon = {
                        if (navigationAction != null) {
                            navigationAction()
                        }
                    },
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SettlementPreviewSummaryCard(summary = summary)
                }
                item {
                    SettlementTransfersCard(
                        transfers = transfers,
                        trackedUpiResponses = trackedUpiResponses,
                        onTransferUpiPayClick = onTransferUpiPayClick,
                    )
                }
                item {
                    SettlementConfirmationCard(
                        isOwner = isOwner,
                        hasExpenses = hasExpenses,
                        isFinalizing = isFinalizing,
                        onConfirm = onConfirm,
                    )
                }
                if (footerContent != null) {
                    item {
                        footerContent()
                    }
                }
            }
        }

        if (successState != null) {
            SettlementSuccessCelebration(
                successState = successState,
                isVisible = isSuccessVisible,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun SettlementPreviewSummaryCard(
    summary: SettlementPreviewSummaryUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MintGlow),
        shadowElevation = 22.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Night, NightSoft, Charcoal),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionLabel(text = "Settlement Preview")
            Text(
                text = summary.groupName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextOnDark,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Review the final snapshot before the group is reset for a fresh cycle.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnDark.copy(alpha = 0.76f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettlementMetricCard(
                    label = "Active members",
                    value = summary.activeMembersCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                SettlementMetricCard(
                    label = "Expense entries",
                    value = summary.expenseEntriesCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettlementMetricCard(
                    label = "Total amount",
                    value = summary.totalAmountDisplay,
                    valueColor = MintGreen,
                    modifier = Modifier.weight(1f),
                )
                SettlementMetricCard(
                    label = "Open balances",
                    value = summary.openBalancesCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun SettlementTransfersCard(
    transfers: List<SettlementTransferUi>,
    trackedUpiResponses: List<SettlementTrackedUpiResponseUi>,
    onTransferUpiPayClick: (SettlementTransferUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettlementSectionCard(
            title = "Suggested Transfers",
            supportingText = if (transfers.isEmpty()) {
                "No transfer is needed. Balances are already settled."
            } else {
                "These suggested payments clear the current imbalance with the fewest steps."
            },
        ) {
            if (transfers.isEmpty()) {
                SettlementHintSurface(
                    text = "This group is already balanced, so no payment handoff is needed.",
                )
            } else {
                transfers.forEach { transfer ->
                    SettlementTransferRow(
                        transfer = transfer,
                        modifier = Modifier.fillMaxWidth(),
                        onPayViaUpiClick = {
                            onTransferUpiPayClick(transfer)
                        },
                    )
                }
            }
        }

        if (trackedUpiResponses.isNotEmpty()) {
            SettlementTrackedUpiResponsesCard(
                responses = trackedUpiResponses,
            )
        }
    }
}

@Composable
private fun SettlementTrackedUpiResponsesCard(
    responses: List<SettlementTrackedUpiResponseUi>,
    modifier: Modifier = Modifier,
) {
    SettlementSectionCard(
        title = "Tracked UPI Responses",
        supportingText = "Responses captured for this group cycle. They stay in the ledger and settlement PDF, while final accounting is completed only during owner settlement.",
        modifier = modifier,
    ) {
        responses.forEach { response ->
            SettlementTrackedUpiResponseRow(
                response = response,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettlementTrackedUpiResponseRow(
    response: SettlementTrackedUpiResponseUi,
    modifier: Modifier = Modifier,
) {
    val tone = when (response.status) {
        SettlementUpiStatus.SUCCESS -> SettlementTone(
            label = response.status.displayLabel,
            accent = MintGreen,
            container = MintGlow,
        )
        SettlementUpiStatus.CANCELLED -> SettlementTone(
            label = response.status.displayLabel,
            accent = CoralSoft,
            container = CoralSoft.copy(alpha = 0.18f),
        )
        SettlementUpiStatus.PENDING -> SettlementTone(
            label = response.status.displayLabel,
            accent = MintTeal,
            container = MintTeal.copy(alpha = 0.18f),
        )
        SettlementUpiStatus.FAILED -> SettlementTone(
            label = response.status.displayLabel,
            accent = CoralSoft,
            container = CoralSoft.copy(alpha = 0.18f),
        )
        SettlementUpiStatus.UNKNOWN -> SettlementTone(
            label = response.status.displayLabel,
            accent = TextOnDark,
            container = TextOnDark.copy(alpha = 0.10f),
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = TextOnDark.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${response.payerName} -> ${response.receiverName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextOnDark,
                    fontWeight = FontWeight.SemiBold,
                )
                SettlementStatusBadge(
                    text = tone.label,
                    accentColor = tone.accent,
                    containerColor = tone.container,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = response.handledAtDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDark.copy(alpha = 0.70f),
                )
                Text(
                    text = response.amountDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    color = MintGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (response.referenceDisplay.isNotBlank()) {
                Text(
                    text = response.referenceDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDark.copy(alpha = 0.70f),
                )
            }
        }
    }
}

@Composable
fun SettlementConfirmationCard(
    isOwner: Boolean,
    hasExpenses: Boolean,
    isFinalizing: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusTone = when {
        !isOwner -> SettlementTone(
            label = "Owner access only",
            accent = CoralSoft,
            container = CoralSoft.copy(alpha = 0.18f),
        )
        !hasExpenses -> SettlementTone(
            label = "No expenses",
            accent = TextOnDark.copy(alpha = 0.82f),
            container = TextOnDark.copy(alpha = 0.10f),
        )
        isFinalizing -> SettlementTone(
            label = "Finalizing",
            accent = MintGreen,
            container = MintGlow,
        )
        else -> SettlementTone(
            label = "Ready to settle",
            accent = MintGreen,
            container = MintGlow,
        )
    }

    SettlementSectionCard(
        title = "Confirmation",
        supportingText = "Generate the settlement PDF, dispatch it to members, and clear the current cycle only after you review the transfer plan.",
        modifier = modifier,
        badge = {
            SettlementStatusBadge(
                text = statusTone.label,
                accentColor = statusTone.accent,
                containerColor = statusTone.container,
            )
        },
    ) {
        when {
            !isOwner -> SettlementHintSurface(
                text = "Only an owner can confirm settlement for this group.",
                accentColor = CoralSoft,
            )
            !hasExpenses -> SettlementHintSurface(
                text = "No expenses are available to settle in this group yet.",
            )
        }

        GradientButton(
            text = if (isFinalizing) "Finalizing..." else "Generate PDF & Settle",
            onClick = onConfirm,
            enabled = !isFinalizing && isOwner && hasExpenses,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isFinalizing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MintGreen,
                    trackColor = TextOnDark.copy(alpha = 0.14f),
                )
                Text(
                    text = "Please keep the app open while the PDF is generated and the group is settled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDark.copy(alpha = 0.74f),
                )
            }
        }
    }
}

@Composable
private fun SettlementSectionCard(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.70f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Charcoal, NightSoft, Night),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SectionLabel(text = title)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextOnDark,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDark.copy(alpha = 0.76f),
                    )
                }
                if (badge != null) {
                    Box(
                        modifier = Modifier.padding(start = 12.dp),
                    ) {
                        badge()
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun SettlementMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextOnDark,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = TextOnDark.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextOnDark.copy(alpha = 0.70f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SettlementTransferRow(
    transfer: SettlementTransferUi,
    modifier: Modifier = Modifier,
    onPayViaUpiClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = TextOnDark.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarBadge(label = transfer.payerName, size = 36.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Payer",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOnDark.copy(alpha = 0.64f),
                        )
                        Text(
                            text = transfer.payerName,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextOnDark,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Pays",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnDark.copy(alpha = 0.64f),
                    )
                    SettlementStatusBadge(
                        text = transfer.amountDisplay,
                        accentColor = MintGreen,
                        containerColor = MintGlow,
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Receiver",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOnDark.copy(alpha = 0.64f),
                        )
                        Text(
                            text = transfer.receiverName,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextOnDark,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(modifier = Modifier.padding(start = 10.dp)) {
                        AvatarBadge(label = transfer.receiverName, size = 36.dp)
                    }
                }
            }

            if (transfer.receiverUpiId.isNotBlank() && transfer.canPayViaUpi) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    SettlementActionChip(
                        text = "Pay via UPI",
                        onClick = onPayViaUpiClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementHintSurface(
    text: String,
    accentColor: Color = TextOnDark.copy(alpha = 0.84f),
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = TextOnDark.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.10f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun SettlementStatusBadge(
    text: String,
    accentColor: Color,
    containerColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun SettlementActionChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MintGlow,
        border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.24f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MintGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

private data class SettlementTone(
    val label: String,
    val accent: Color,
    val container: Color,
)

@Composable
private fun SettlementSuccessCelebration(
    successState: SettlementSuccessUi,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "settlement_success_overlay_alpha",
    )
    val haloScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.68f,
        animationSpec = tween(durationMillis = 520),
        label = "settlement_success_halo_scale",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 360),
        label = "settlement_success_card_scale",
    )

    val transferBadge = if (successState.transferCount == 0) {
        "Already balanced"
    } else {
        "${successState.transferCount} transfer" +
            if (successState.transferCount == 1) " cleared" else "s cleared"
    }

    Box(
        modifier = modifier
            .background(Night.copy(alpha = 0.74f))
            .graphicsLayer(alpha = overlayAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer(
                        alpha = 0.9f,
                        scaleX = haloScale,
                        scaleY = haloScale,
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MintGreen.copy(alpha = 0.32f),
                                MintTeal.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                        shape = RoundedCornerShape(120.dp),
                    ),
            )

            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .graphicsLayer(
                        alpha = overlayAlpha,
                        scaleX = cardScale,
                        scaleY = cardScale,
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.92f)),
                shadowElevation = 22.dp,
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Night, NightSoft, Charcoal),
                            ),
                        )
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MintGlow,
                        border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.26f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.titleLarge,
                                color = MintGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Settlement complete",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextOnDark,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${successState.groupName} is ready for a fresh cycle. Sharing the final PDF now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnDark.copy(alpha = 0.78f),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SettlementStatusBadge(
                            text = transferBadge,
                            accentColor = MintGreen,
                            containerColor = MintGlow,
                        )
                        SettlementStatusBadge(
                            text = successState.totalAmountDisplay,
                            accentColor = MintTeal,
                            containerColor = MintTeal.copy(alpha = 0.16f),
                        )
                    }

                    Text(
                        text = if (isVisible) "Wrapping up..." else "Opening share sheet...",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextOnDark.copy(alpha = 0.64f),
                    )
                    Box(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
