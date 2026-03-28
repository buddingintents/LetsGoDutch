package com.buddingintents.letsgodutch

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun PersonalExpenseTrackerScreen(
    expenses: List<PersonalExpenseEntry>,
    onDeleteExpense: (PersonalExpenseEntry) -> Unit,
    onExportPdf: (filteredExpenses: List<PersonalExpenseEntry>, filterDescription: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var minAmountText by rememberSaveable { mutableStateOf("") }
    var selectedPeriod by rememberSaveable { mutableStateOf(PersonalExpensePeriodFilter.ALL) }
    var exportMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val sortedExpenses = expenses.sortedByDescending { it.spentAtEpochMs }
    val today = LocalDate.now()
    val minAmountPaise = minAmountText.toPaise()
    val normalizedQuery = searchQuery.trim()

    val filteredExpenses = sortedExpenses.filter { expense ->
        val matchesPeriod = expense.matchesPeriod(
            filter = selectedPeriod,
            today = today,
        )
        val matchesQuery = normalizedQuery.isBlank() ||
            expense.title.contains(normalizedQuery, ignoreCase = true)
        val matchesMinAmount = (minAmountPaise ?: 0L) <= 0L || expense.amountPaise >= (minAmountPaise ?: 0L)
        matchesPeriod && matchesQuery && matchesMinAmount
    }

    val totalPaise = filteredExpenses.sumOf { it.amountPaise }
    val averagePaise = if (filteredExpenses.isNotEmpty()) totalPaise / filteredExpenses.size else 0L
    val highestExpense = filteredExpenses.maxByOrNull { it.amountPaise }
    val monthlyReport = filteredExpenses
        .groupBy { entry -> YearMonth.from(entry.spentAtEpochMs.toLocalDate()) }
        .map { (month, entriesForMonth) ->
            MonthlyReportRow(
                month = month,
                totalPaise = entriesForMonth.sumOf { it.amountPaise },
                count = entriesForMonth.size,
            )
        }
        .sortedByDescending { it.month }
    val maxMonthlyTotal = monthlyReport.maxOfOrNull { it.totalPaise }?.coerceAtLeast(1L) ?: 1L

    val filterDescription = buildFilterDescription(
        period = selectedPeriod,
        searchQuery = normalizedQuery,
        minAmountPaise = minAmountPaise,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Box {
                        IconButton(onClick = { exportMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                            )
                        }
                        DropdownMenu(
                            expanded = exportMenuExpanded,
                            onDismissRequest = { exportMenuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export PDF") },
                                enabled = filteredExpenses.isNotEmpty(),
                                onClick = {
                                    exportMenuExpanded = false
                                    onExportPdf(filteredExpenses, filterDescription)
                                },
                            )
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PersonalExpensePeriodFilter.entries, key = { it.name }) { filter ->
                        FilterChip(
                            selected = selectedPeriod == filter,
                            onClick = { selectedPeriod = filter },
                            label = { Text(filter.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = minAmountText,
                    onValueChange = { minAmountText = it },
                    label = { Text("Min amount (\u20B9)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Report Summary",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = totalPaise.toInrDisplay(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "${filteredExpenses.size} entries | Avg ${averagePaise.toInrDisplay()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = "Highest: ${highestExpense?.amountPaise?.toInrDisplay() ?: "\u20B90.00"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No expenses match this filter.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (monthlyReport.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Monthly Report",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                monthlyReport.take(6).forEach { monthRow ->
                                    val progress = monthRow.totalPaise.toFloat() / maxMonthlyTotal.toFloat()
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = monthRow.month.format(monthFormatter),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            Text(
                                                text = "${monthRow.totalPaise.toInrDisplay()} (${monthRow.count})",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(filteredExpenses, key = { it.expenseId }) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                            ) {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = expense.spentAtEpochMs.toReadableExpenseDateTime(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = expense.amountPaise.toInrDisplay(),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            IconButton(onClick = { onDeleteExpense(expense) }) {
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

        LetsGoDutchBannerAd(
            productionAdUnitId = PERSONAL_EXPENSE_BANNER_AD_UNIT_ID,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
        )
    }
}

@Composable
fun AddPersonalExpenseDialog(
    onDismiss: () -> Unit,
    onAddExpense: (title: String, amountPaise: Long, spentAtEpochMs: Long) -> Unit,
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var expenseDate by rememberSaveable { mutableStateOf(today.format(dialogDateFormatter)) }

    val amountPaise = amountText.toPaise()
    val parsedExpenseDate = runCatching {
        LocalDate.parse(expenseDate, dialogDateFormatter)
    }.getOrNull()
    val isValid = title.trim().isNotBlank() &&
        (amountPaise ?: 0L) > 0L &&
        parsedExpenseDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Personal Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (\u20B9)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = expenseDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expense date") },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                val current = parsedExpenseDate ?: today
                                val picker = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                        if (!selected.isAfter(today)) {
                                            expenseDate = selected.format(dialogDateFormatter)
                                        }
                                    },
                                    current.year,
                                    current.monthValue - 1,
                                    current.dayOfMonth,
                                )
                                picker.datePicker.maxDate = System.currentTimeMillis()
                                picker.show()
                            },
                        ) {
                            Text("Select")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = parsedExpenseDate ?: today
                    onAddExpense(
                        title.trim(),
                        amountPaise ?: 0L,
                        selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    )
                },
                enabled = isValid,
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private data class MonthlyReportRow(
    val month: YearMonth,
    val totalPaise: Long,
    val count: Int,
)

private enum class PersonalExpensePeriodFilter(val label: String) {
    ALL("All"),
    THIS_MONTH("This month"),
    LAST_30_DAYS("Last 30 days"),
    THIS_YEAR("This year"),
}

private fun PersonalExpenseEntry.matchesPeriod(
    filter: PersonalExpensePeriodFilter,
    today: LocalDate,
): Boolean {
    val spentDate = spentAtEpochMs.toLocalDate()
    return when (filter) {
        PersonalExpensePeriodFilter.ALL -> true
        PersonalExpensePeriodFilter.THIS_MONTH -> YearMonth.from(spentDate) == YearMonth.from(today)
        PersonalExpensePeriodFilter.LAST_30_DAYS -> !spentDate.isBefore(today.minusDays(29))
        PersonalExpensePeriodFilter.THIS_YEAR -> spentDate.year == today.year
    }
}

private fun buildFilterDescription(
    period: PersonalExpensePeriodFilter,
    searchQuery: String,
    minAmountPaise: Long?,
): String {
    val parts = mutableListOf<String>()
    parts += "Period: ${period.label}"
    if (searchQuery.isNotBlank()) {
        parts += "Search: \"$searchQuery\""
    }
    if ((minAmountPaise ?: 0L) > 0L) {
        parts += "Min amount: ${(minAmountPaise ?: 0L).toInrDisplay()}"
    }
    return parts.joinToString(" | ")
}

private val expenseDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)

private val dialogDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)

private val monthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

private const val PERSONAL_EXPENSE_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/9209393998"

private fun Long.toReadableExpenseDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(expenseDateFormatter)
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun Long.toInrDisplay(): String {
    val absPaise = abs(this)
    val rupees = absPaise / 100
    val paise = absPaise % 100
    val prefix = if (this < 0L) "-\u20B9" else "\u20B9"
    return "$prefix$rupees.${paise.toString().padStart(2, '0')}"
}

private fun String.toPaise(): Long? {
    val normalized = trim()
    if (normalized.isEmpty()) return null
    val rupees = normalized.toBigDecimalOrNull() ?: return null
    return rupees
        .multiply(BigDecimal(100))
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}
