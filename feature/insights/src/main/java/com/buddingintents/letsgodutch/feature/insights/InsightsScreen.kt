package com.buddingintents.letsgodutch.feature.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.CoralSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.Night
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.ExpenseCategory
import com.buddingintents.letsgodutch.core.model.Money
import com.buddingintents.letsgodutch.core.model.SettlementUpiStatus
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class InsightSettlementTransferUi(
    val transferKey: String,
    val payerUserId: String,
    val payerName: String,
    val receiverUserId: String,
    val receiverName: String,
    val receiverUpiId: String,
    val amountPaise: Long,
    val amountDisplay: String,
    val canPayViaUpi: Boolean,
)

data class InsightTrackedSettlementActivityUi(
    val activityId: String,
    val payerName: String,
    val receiverName: String,
    val amountDisplay: String,
    val status: SettlementUpiStatus,
    val handledAtDisplay: String,
    val referenceDisplay: String = "",
)

@Composable
fun InsightsScreen(
    balances: List<Balance>,
    memberNameById: Map<String, String>,
    expenses: List<Expense>,
    totalExpensePaise: Long,
    settlementTransfers: List<InsightSettlementTransferUi>,
    trackedActivities: List<InsightTrackedSettlementActivityUi>,
    onSuggestedTransferPayClick: (InsightSettlementTransferUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenBrush = Brush.verticalGradient(
        colors = listOf(Night, NightSoft, Charcoal),
    )
    val panelBorder = MintTeal.copy(alpha = 0.24f)
    val panelSurface = TextOnDark.copy(alpha = 0.06f)
    val supportingTextColor = TextOnDark.copy(alpha = 0.72f)
    val positiveTone = InsightTone(
        label = "Receivable",
        accentColor = MintGreen,
        containerColor = MintGreen.copy(alpha = 0.16f),
    )
    val negativeTone = InsightTone(
        label = "Payable",
        accentColor = MaterialTheme.colorScheme.error,
        containerColor = CoralSoft.copy(alpha = 0.16f),
    )
    val settledTone = InsightTone(
        label = "Settled",
        accentColor = TextOnDark.copy(alpha = 0.62f),
        containerColor = TextOnDark.copy(alpha = 0.10f),
    )
    val entries = balances
        .map { balance ->
            MemberInsight(
                userId = balance.userId,
                name = memberNameById[balance.userId].orEmpty().ifBlank { "Member" },
                netPaise = balance.netPaise,
            )
        }
        .sortedByDescending { abs(it.netPaise) }

    val receives = entries.filter { it.netPaise > 0 }.sortedByDescending { it.netPaise }
    val owes = entries.filter { it.netPaise < 0 }.sortedBy { it.netPaise }
    val settledMembers = entries.filter { it.netPaise == 0L }
    val totalReceivable = receives.sumOf { it.netPaise }
    val totalPayable = owes.sumOf { abs(it.netPaise) }
    val nearZeroCount = entries.count { abs(it.netPaise) <= 100L }
    val largestCreditor = receives.firstOrNull()
    val largestDebtor = owes.firstOrNull()
    val maxAbsBalance = max(1L, entries.maxOfOrNull { abs(it.netPaise) } ?: 1L)
    val chartEntries = entries.filter { it.netPaise != 0L }.ifEmpty { entries }.take(5)
    val categoryBreakdown = expenses
        .groupBy { it.category }
        .map { (category, categoryExpenses) ->
            InsightCategoryBreakdown(
                category = category,
                totalPaise = categoryExpenses.sumOf { it.amountPaise },
                expenseCount = categoryExpenses.size,
            )
        }
        .sortedByDescending { it.totalPaise }
    val showCategoryBreakdown = categoryBreakdown.size >= 2
    val topCategoryTotal = max(1L, categoryBreakdown.maxOfOrNull { it.totalPaise } ?: 1L)
    val distributionSegments = listOf(
        InsightChartSegment(
            label = positiveTone.label,
            value = receives.size.toFloat(),
            tone = positiveTone,
        ),
        InsightChartSegment(
            label = negativeTone.label,
            value = owes.size.toFloat(),
            tone = negativeTone,
        ),
        InsightChartSegment(
            label = settledTone.label,
            value = settledMembers.size.toFloat(),
            tone = settledTone,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBrush),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                InsightPanel(
                    eyebrow = "Overview",
                    title = "Group Totals",
                    supportingText = if (entries.isEmpty()) {
                        "Add expenses to unlock insights for this group."
                    } else {
                        "$nearZeroCount of ${entries.size} members are already close to settled."
                    },
                    borderColor = panelBorder,
                    panelColor = panelSurface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatBlock(
                            label = "Total spend",
                            value = Money(totalExpensePaise).toInrDisplay(),
                            valueColor = TextOnDark,
                            supportingColor = supportingTextColor,
                            modifier = Modifier.weight(1f),
                        )
                        StatBlock(
                            label = "To Be Settled",
                            value = Money(totalReceivable).toInrDisplay(),
                            valueColor = MintGreen,
                            supportingColor = supportingTextColor,
                            alignEnd = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                InsightPanel(
                    eyebrow = "Snapshot",
                    title = "Settlement Distribution",
                    supportingText = if (entries.isEmpty()) {
                        "No member balance data is available yet."
                    } else {
                        "Live chart of who still needs to receive, pay, or is already settled."
                    },
                    borderColor = panelBorder,
                    panelColor = panelSurface,
                ) {
                    if (entries.isEmpty()) {
                        Text(
                            text = "Add expenses to populate the chart and legend.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = supportingTextColor,
                        )
                    } else {
                        InsightDonutChart(
                            segments = distributionSegments,
                            centerValue = entries.size.toString(),
                            centerLabel = "members",
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InsightLegendRow(
                                label = positiveTone.label,
                                detail = if (receives.isEmpty()) {
                                    "No incoming dues"
                                } else {
                                    Money(totalReceivable).toInrDisplay()
                                },
                                value = "${receives.size} members",
                                tone = positiveTone,
                            )
                            InsightLegendRow(
                                label = negativeTone.label,
                                detail = if (owes.isEmpty()) {
                                    "No outgoing dues"
                                } else {
                                    Money(totalPayable).toInrDisplay()
                                },
                                value = "${owes.size} members",
                                tone = negativeTone,
                            )
                            InsightLegendRow(
                                label = settledTone.label,
                                detail = if (settledMembers.isEmpty()) {
                                    "No fully settled members yet"
                                } else {
                                    "Net zero balance"
                                },
                                value = "${settledMembers.size} members",
                                tone = settledTone,
                            )
                        }
                    }
                }
            }

            if (showCategoryBreakdown) {
                item {
                    InsightPanel(
                        eyebrow = "Spend Mix",
                        title = "Category Breakdown",
                        supportingText = "Expense totals grouped by category for this group.",
                        borderColor = panelBorder,
                        panelColor = panelSurface,
                    ) {
                        categoryBreakdown.forEach { entry ->
                            CategoryBreakdownRow(
                                item = entry,
                                fraction = (entry.totalPaise.toFloat() / topCategoryTotal.toFloat())
                                    .coerceIn(0.08f, 1f),
                            )
                        }
                    }
                }
            }

            item {
                InsightPanel(
                    eyebrow = "Movement",
                    title = "Who Should Pay / Receive",
                    borderColor = panelBorder,
                    panelColor = panelSurface,
                ) {
                    InsightLeaderRow(
                        title = "Top Receiver",
                        item = largestCreditor,
                        fallback = "No outstanding receiver.",
                        tone = positiveTone,
                        supportingColor = supportingTextColor,
                    )
                    InsightLeaderRow(
                        title = "Top Payer",
                        item = largestDebtor,
                        fallback = "No outstanding payer.",
                        tone = negativeTone,
                        supportingColor = supportingTextColor,
                    )
                }
            }

            item {
                InsightPanel(
                    eyebrow = "Balances",
                    title = "Member Balance Radar",
                    supportingText = if (chartEntries.isEmpty()) {
                        null
                    } else {
                        "Top open balances are charted first, with the full member list below."
                    },
                    borderColor = panelBorder,
                    panelColor = panelSurface,
                ) {
                    if (entries.isEmpty()) {
                        Text(
                            text = "No balance data yet. Add expenses to unlock insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = supportingTextColor,
                        )
                    } else {
                        InsightBalanceBarChart(
                            items = chartEntries,
                            maxAbsBalance = maxAbsBalance,
                            positiveTone = positiveTone,
                            negativeTone = negativeTone,
                            settledTone = settledTone,
                        )
                        entries.forEach { item ->
                            val fraction = (abs(item.netPaise).toFloat() / maxAbsBalance.toFloat())
                                .coerceIn(0.04f, 1f)
                            val tone = when {
                                item.netPaise > 0 -> positiveTone
                                item.netPaise < 0 -> negativeTone
                                else -> settledTone
                            }
                            InsightBalanceRow(
                                item = item,
                                fraction = fraction,
                                tone = tone,
                            )
                        }
                    }
                }
            }

            item {
                InsightPanel(
                    eyebrow = "Next Step",
                    title = "Suggested Transfers",
                    borderColor = panelBorder,
                    panelColor = panelSurface,
                ) {
                    if (settlementTransfers.isEmpty()) {
                        Text(
                            text = recommendedAction(
                                topDebtor = largestDebtor,
                                topCreditor = largestCreditor,
                                outstandingPaise = totalReceivable,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = supportingTextColor,
                        )
                    } else {
                        settlementTransfers.take(8).forEach { suggestion ->
                            SuggestedTransferRow(
                                suggestion = suggestion,
                                onPayViaUpiClick = { onSuggestedTransferPayClick(suggestion) },
                            )
                        }
                        if (settlementTransfers.size > 8) {
                            Text(
                                text = "+${settlementTransfers.size - 8} more suggestions",
                                style = MaterialTheme.typography.bodySmall,
                                color = supportingTextColor,
                            )
                        }
                    }
                }
            }

            if (trackedActivities.isNotEmpty()) {
                item {
                    InsightPanel(
                        eyebrow = "Activity",
                        title = "Recorded Payment Activity",
                        borderColor = panelBorder,
                        panelColor = panelSurface,
                    ) {
                        trackedActivities.take(6).forEach { activity ->
                            TrackedActivityRow(activity = activity)
                        }
                        if (trackedActivities.size > 6) {
                            Text(
                                text = "+${trackedActivities.size - 6} more events",
                                style = MaterialTheme.typography.bodySmall,
                                color = supportingTextColor,
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = panelSurface,
                    border = BorderStroke(1.dp, panelBorder),
                ) {
                    InsightsBannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightPanel(
    eyebrow: String,
    title: String,
    supportingText: String? = null,
    borderColor: Color,
    panelColor: Color,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            NightSoft.copy(alpha = 0.96f),
                            Night.copy(alpha = 0.98f),
                            Charcoal.copy(alpha = 0.96f),
                        ),
                    ),
                )
                .background(panelColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionLabel(text = eyebrow)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextOnDark,
            )
            if (!supportingText.isNullOrBlank()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDark.copy(alpha = 0.72f),
                )
            }
            content()
        }
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    valueColor: Color,
    supportingColor: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = supportingColor,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
    }
}

@Composable
private fun InsightDonutChart(
    segments: List<InsightChartSegment>,
    centerValue: String,
    centerLabel: String,
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    val trackColor = TextOnDark.copy(alpha = 0.10f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(184.dp),
        ) {
            val strokeWidth = 26.dp.toPx()
            val arcSize = Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth,
            )
            val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            if (total > 0f) {
                var startAngle = -90f
                segments.filter { it.value > 0f }.forEach { segment ->
                    val sweepAngle = 360f * (segment.value / total)
                    drawArc(
                        color = segment.tone.accentColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = centerValue,
                style = MaterialTheme.typography.headlineSmall,
                color = TextOnDark,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelMedium,
                color = TextOnDark.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun InsightLegendRow(
    label: String,
    detail: String,
    value: String,
    tone: InsightTone,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, tone.containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(tone.accentColor),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextOnDark,
                    )
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDark.copy(alpha = 0.68f),
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = tone.accentColor,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun InsightLeaderRow(
    title: String,
    item: MemberInsight?,
    fallback: String,
    tone: InsightTone,
    supportingColor: Color,
) {
    if (item == null) {
        Text(
            text = "$title: $fallback",
            style = MaterialTheme.typography.bodyMedium,
            color = supportingColor,
        )
        return
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, tone.containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = supportingColor,
                )
                InsightToneChip(
                    label = tone.label,
                    tone = tone,
                )
            }
            Text(
                text = "${item.name} (${Money(item.netPaise).toInrDisplay()})",
                style = MaterialTheme.typography.bodyMedium,
                color = tone.accentColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InsightBalanceBarChart(
    items: List<MemberInsight>,
    maxAbsBalance: Long,
    positiveTone: InsightTone,
    negativeTone: InsightTone,
    settledTone: InsightTone,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Net balance chart",
            style = MaterialTheme.typography.labelLarge,
            color = TextOnDark.copy(alpha = 0.72f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(198.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            items.forEach { item ->
                val tone = when {
                    item.netPaise > 0 -> positiveTone
                    item.netPaise < 0 -> negativeTone
                    else -> settledTone
                }
                val fraction = (abs(item.netPaise).toFloat() / maxAbsBalance.toFloat())
                    .coerceIn(0.08f, 1f)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = Money(item.netPaise).toInrDisplay(),
                        style = MaterialTheme.typography.labelSmall,
                        color = tone.accentColor,
                        textAlign = TextAlign.Center,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.80f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(tone.containerColor),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.80f)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            tone.accentColor.copy(alpha = 0.40f),
                                            tone.accentColor,
                                        ),
                                    ),
                                ),
                        )
                    }
                    Text(
                        text = shortChartLabel(item.name),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextOnDark,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightBalanceRow(
    item: MemberInsight,
    fraction: Float,
    tone: InsightTone,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, tone.containerColor),
    ) {
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
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextOnDark,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InsightToneChip(
                        label = tone.label,
                        tone = tone,
                    )
                    Text(
                        text = Money(item.netPaise).toInrDisplay(),
                        style = MaterialTheme.typography.labelLarge,
                        color = tone.accentColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(96.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tone.containerColor),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tone.accentColor),
                )
            }
        }
    }
}

@Composable
private fun InsightToneChip(
    label: String,
    tone: InsightTone,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tone.containerColor,
        border = BorderStroke(1.dp, tone.accentColor.copy(alpha = 0.20f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tone.accentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

private fun recommendedAction(
    topDebtor: MemberInsight?,
    topCreditor: MemberInsight?,
    outstandingPaise: Long,
): String {
    if (outstandingPaise <= 0L) {
        return "Everything looks settled. Start adding new expenses for the next cycle."
    }
    if (topDebtor == null || topCreditor == null) {
        return "Keep adding expenses. Suggestions will appear once payables and receivables diverge."
    }
    val amount = min(abs(topDebtor.netPaise), topCreditor.netPaise)
    return "${topDebtor.name} can settle ${Money(amount).toInrDisplay()} with ${topCreditor.name} to reduce most of the imbalance quickly."
}

@Composable
private fun SuggestedTransferRow(
    suggestion: InsightSettlementTransferUi,
    onPayViaUpiClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MintTeal.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${suggestion.payerName} pays ${suggestion.amountDisplay} to ${suggestion.receiverName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnDark.copy(alpha = 0.76f),
            )
            if (suggestion.canPayViaUpi && suggestion.receiverUpiId.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onPayViaUpiClick),
                        shape = RoundedCornerShape(999.dp),
                        color = MintGreen.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.28f)),
                    ) {
                        Text(
                            text = "Pay via UPI",
                            style = MaterialTheme.typography.labelMedium,
                            color = MintGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackedActivityRow(
    activity: InsightTrackedSettlementActivityUi,
) {
    val statusColor = when (activity.status) {
        SettlementUpiStatus.SUCCESS -> MintGreen
        SettlementUpiStatus.PENDING -> MintTeal
        SettlementUpiStatus.CANCELLED,
        SettlementUpiStatus.FAILED,
        -> MaterialTheme.colorScheme.error
        SettlementUpiStatus.UNKNOWN -> TextOnDark.copy(alpha = 0.62f)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${activity.payerName} -> ${activity.receiverName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDark,
                )
                Text(
                    text = activity.amountDisplay,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnDark,
                )
            }
            Text(
                text = "${activity.status.displayLabel} • ${activity.handledAtDisplay}",
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
            )
            if (activity.referenceDisplay.isNotBlank()) {
                Text(
                    text = activity.referenceDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDark.copy(alpha = 0.70f),
                )
            }
        }
    }
}

private fun shortChartLabel(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        ?.take(6)
        ?.ifBlank { null }
        ?: "Member"

private data class MemberInsight(
    val userId: String,
    val name: String,
    val netPaise: Long,
)

private data class InsightTone(
    val label: String,
    val accentColor: Color,
    val containerColor: Color,
)

private data class InsightChartSegment(
    val label: String,
    val value: Float,
    val tone: InsightTone,
)

private data class InsightCategoryBreakdown(
    val category: ExpenseCategory,
    val totalPaise: Long,
    val expenseCount: Int,
)

@Composable
private fun CategoryBreakdownRow(
    item: InsightCategoryBreakdown,
    fraction: Float,
) {
    val accentColor = categoryAccentColor(item.category)
    val containerColor = accentColor.copy(alpha = 0.16f)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = TextOnDark.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, containerColor),
    ) {
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.category.displayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextOnDark,
                    )
                    Text(
                        text = "${item.expenseCount} expense${if (item.expenseCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDark.copy(alpha = 0.68f),
                    )
                }
                Text(
                    text = Money(item.totalPaise).toInrDisplay(),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    textAlign = TextAlign.End,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor),
                )
            }
        }
    }
}

private fun categoryAccentColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FOOD -> MintGreen
        ExpenseCategory.TRAVEL -> MintTeal
        ExpenseCategory.STAY -> Color(0xFF6CC3FF)
        ExpenseCategory.FUN -> Color(0xFFFFC857)
        ExpenseCategory.OTHER -> TextOnDark.copy(alpha = 0.72f)
    }
}

@Composable
private fun InsightsBannerAd(
    modifier: Modifier = Modifier,
) {
    val adWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    key(adWidthDp) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    adUnitId = INSIGHTS_BANNER_AD_UNIT_ID
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

private const val INSIGHTS_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/6251098676"
