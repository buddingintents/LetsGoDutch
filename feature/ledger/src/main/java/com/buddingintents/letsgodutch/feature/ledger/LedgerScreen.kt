package com.buddingintents.letsgodutch.feature.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Money
import com.buddingintents.letsgodutch.core.model.SettlementUpiTransaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LedgerScreen(
    expenses: List<Expense>,
    settlementActivities: List<SettlementUpiTransaction>,
    memberNameById: Map<String, String>,
    memberPhotoUrlById: Map<String, String?>,
    allowDelete: Boolean = false,
    onDeleteExpenseClick: ((Expense) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val titleWeight = 1.35f
    val paidByWeight = 1.0f
    val amountWeight = 0.8f
    val actionColumnWidth = 36.dp

    Column(modifier = modifier.padding(horizontal = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Title",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(titleWeight),
            )
            Text(
                text = "Paid By",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(paidByWeight),
            )
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(amountWeight),
            )
            if (allowDelete) {
                Row(
                    modifier = Modifier.width(actionColumnWidth),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(" ")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.expenseId }) { expense ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val paidByName = memberNameById[expense.paidByUserId]
                            ?.ifBlank { "Member" }
                            ?: "Member"
                        val paidByPhoto = memberPhotoUrlById[expense.paidByUserId]
                        Column(
                            modifier = Modifier.weight(titleWeight),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = expense.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = expense.paymentDate.toUiPaymentDate(
                                    fallbackEpochMs = expense.createdAtEpochMs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                            if (expense.note.isNotBlank()) {
                                Text(
                                    text = expense.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.weight(paidByWeight),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MemberAvatar(
                                displayName = paidByName,
                                photoUrl = paidByPhoto,
                            )
                            Text(
                                text = paidByName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = Money(expense.amountPaise).toRupeeDisplay(),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(amountWeight),
                        )
                        if (allowDelete) {
                            Row(
                                modifier = Modifier.width(actionColumnWidth),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    modifier = Modifier.size(32.dp),
                                    onClick = { onDeleteExpenseClick?.invoke(expense) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete expense",
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (settlementActivities.isNotEmpty()) {
                item {
                    Text(
                        text = "Recorded Payments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    )
                }

                items(
                    settlementActivities,
                    key = { activity -> activity.activityId.ifBlank { "${activity.transferKey}_${activity.handledAtEpochMs}" } },
                ) { activity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${activity.payerName.ifBlank { "Member" }} -> ${activity.receiverName.ifBlank { "Member" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = Money(activity.amountPaise).toRupeeDisplay(),
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.End,
                                )
                            }
                            Text(
                                text = "${activity.status.displayLabel} | ${activity.handledAtEpochMs.toLedgerDateTime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val reference = buildSettlementReference(activity)
                            if (reference.isNotBlank()) {
                                Text(
                                    text = reference,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                LedgerBannerAd(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                )
            }
        }
    }
}

private val backendDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
private val uiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM", Locale.US)
private val ledgerDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.US)

private fun String.toUiPaymentDate(fallbackEpochMs: Long): String {
    val parsed = runCatching { LocalDate.parse(this.trim(), backendDateFormatter) }.getOrNull()
    val date = parsed ?: Instant.ofEpochMilli(fallbackEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return date.format(uiDateFormatter)
}

private fun Long.toLedgerDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(ledgerDateTimeFormatter)
}

private fun buildSettlementReference(activity: SettlementUpiTransaction): String {
    val parts = mutableListOf<String>()
    activity.paymentAppName.takeIf { it.isNotBlank() }?.let { parts += "App: $it" }
    if (activity.statusConfirmedByUser) {
        parts += "User confirmed"
    }
    activity.bestReference.takeIf { it.isNotBlank() }?.let { parts += "Ref: $it" }
    activity.responseCode.takeIf { it.isNotBlank() }?.let { parts += "Code: $it" }
    return parts.joinToString(" | ")
}

@Composable
private fun MemberAvatar(
    displayName: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
) {
    val validPhotoUrl = photoUrl?.takeIf { it.isNotBlank() }
    if (!validPhotoUrl.isNullOrBlank()) {
        AsyncImage(
            model = validPhotoUrl,
            contentDescription = "$displayName avatar",
            modifier = modifier
                .size(22.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
        Box(
            modifier = modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LedgerBannerAd(
    modifier: Modifier = Modifier,
) {
    val adWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    key(adWidthDp) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    adUnitId = LEDGER_BANNER_AD_UNIT_ID
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

private const val LEDGER_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/7564180349"
