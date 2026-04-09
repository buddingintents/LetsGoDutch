package com.buddingintents.letsgodutch.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Money
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun InsightsScreen(
    balances: List<Balance>,
    memberNameById: Map<String, String>,
    totalExpensePaise: Long,
    modifier: Modifier = Modifier,
) {
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
    val totalReceivable = receives.sumOf { it.netPaise }
    val nearZeroCount = entries.count { abs(it.netPaise) <= 100L }
    val largestCreditor = receives.firstOrNull()
    val largestDebtor = owes.firstOrNull()
    val maxAbsBalance = max(1L, entries.maxOfOrNull { abs(it.netPaise) } ?: 1L)
    val suggestedTransactions = buildSuggestedTransactions(entries)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Group Totals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            StatBlock(
                                label = "Total spend",
                                value = Money(totalExpensePaise).toInrDisplay(),
                                modifier = Modifier.weight(1f),
                            )
                            StatBlock(
                                label = "To Be Settled",
                                value = Money(totalReceivable).toInrDisplay(),
                                alignEnd = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Text(
                            text = if (entries.isEmpty()) {
                                "Add expenses to unlock insights for this group."
                            } else {
                                "$nearZeroCount of ${entries.size} members are already close to settled."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Who Should Pay / Receive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        InsightLeaderRow(
                            title = "Top Receiver",
                            item = largestCreditor,
                            fallback = "No outstanding receiver.",
                            tint = Color(0xFF168357),
                        )
                        InsightLeaderRow(
                            title = "Top Payer",
                            item = largestDebtor,
                            fallback = "No outstanding payer.",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Member Balance Radar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (entries.isEmpty()) {
                            Text(
                                text = "No balance data yet. Add expenses to unlock insights.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            entries.forEach { item ->
                                val fraction = (abs(item.netPaise).toFloat() / maxAbsBalance.toFloat())
                                    .coerceIn(0.04f, 1f)
                                val barColor = when {
                                    item.netPaise > 0 -> Color(0xFF2E9C6A)
                                    item.netPaise < 0 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.outline
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = Money(item.netPaise).toInrDisplay(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = barColor,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.width(96.dp),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = fraction)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(barColor.copy(alpha = 0.88f)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Suggested Next Step",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (suggestedTransactions.isEmpty()) {
                            Text(
                                text = recommendedAction(
                                    topDebtor = largestDebtor,
                                    topCreditor = largestCreditor,
                                    outstandingPaise = totalReceivable,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            suggestedTransactions.take(8).forEach { suggestion ->
                                val payer = memberNameById[suggestion.fromUserId]
                                    .orEmpty()
                                    .ifBlank { "Member" }
                                val receiver = memberNameById[suggestion.toUserId]
                                    .orEmpty()
                                    .ifBlank { "Member" }
                                Text(
                                    text = "$payer pays ${Money(suggestion.amountPaise).toInrDisplay()} to $receiver",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (suggestedTransactions.size > 8) {
                                Text(
                                    text = "+${suggestedTransactions.size - 8} more suggestions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                InsightsBannerAd(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
    }
}

@Composable
private fun InsightLeaderRow(
    title: String,
    item: MemberInsight?,
    fallback: String,
    tint: Color,
) {
    if (item == null) {
        Text(
            text = "$title: $fallback",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "${item.name} (${Money(item.netPaise).toInrDisplay()})",
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
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

private fun buildSuggestedTransactions(entries: List<MemberInsight>): List<SettlementSuggestion> {
    val creditors = entries
        .filter { it.netPaise > 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = it.netPaise) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()

    val debtors = entries
        .filter { it.netPaise < 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = abs(it.netPaise)) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()

    val suggestions = mutableListOf<SettlementSuggestion>()
    var creditorIndex = 0
    var debtorIndex = 0
    while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
        val creditor = creditors[creditorIndex]
        val debtor = debtors[debtorIndex]
        val amount = min(creditor.amountPaise, debtor.amountPaise)
        if (amount > 0L) {
            suggestions += SettlementSuggestion(
                fromUserId = debtor.userId,
                toUserId = creditor.userId,
                amountPaise = amount,
            )
        }
        creditor.amountPaise -= amount
        debtor.amountPaise -= amount
        if (creditor.amountPaise <= 0L) creditorIndex += 1
        if (debtor.amountPaise <= 0L) debtorIndex += 1
    }
    return suggestions
}

private data class MemberInsight(
    val userId: String,
    val name: String,
    val netPaise: Long,
)

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

private data class MutableSettlementParty(
    val userId: String,
    var amountPaise: Long,
)

private data class SettlementSuggestion(
    val fromUserId: String,
    val toUserId: String,
    val amountPaise: Long,
)
