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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LedgerScreen(
    expenses: List<Expense>,
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
        }
    }
}

private val backendDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
private val uiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM", Locale.US)

private fun String.toUiPaymentDate(fallbackEpochMs: Long): String {
    val parsed = runCatching { LocalDate.parse(this.trim(), backendDateFormatter) }.getOrNull()
    val date = parsed ?: Instant.ofEpochMilli(fallbackEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return date.format(uiDateFormatter)
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
