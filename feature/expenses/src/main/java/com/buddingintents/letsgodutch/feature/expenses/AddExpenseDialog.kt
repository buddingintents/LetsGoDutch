package com.buddingintents.letsgodutch.feature.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.PillTabSelector
import com.buddingintents.letsgodutch.core.model.ExpenseCategory
import com.buddingintents.letsgodutch.core.model.SplitType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExpenseMemberOption(
    val userId: String,
    val displayName: String,
    val photoUrl: String? = null,
)

data class SplitInputDraft(
    val userId: String,
    val value: String,
)

data class ExpenseDraft(
    val title: String,
    val amountRupees: String,
    val paymentDate: String,
    val category: ExpenseCategory,
    val note: String,
    val splitType: SplitType,
    val paidByUserId: String,
    val participantUserIds: List<String>,
    val splitInputs: List<SplitInputDraft>,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    members: List<ExpenseMemberOption>,
    currentUserId: String,
    selectAllMembersByDefaultForExpenses: Boolean,
    onDismiss: () -> Unit,
    onSave: (ExpenseDraft) -> Unit,
) {
    val context = LocalContext.current
    val allMemberIds = remember(members) { members.map { it.userId } }
    val defaultPayer = remember(members, currentUserId) {
        allMemberIds.firstOrNull { it == currentUserId } ?: allMemberIds.firstOrNull().orEmpty()
    }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var note by remember { mutableStateOf("") }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var paidByUserId by remember(defaultPayer) { mutableStateOf(defaultPayer) }
    var participantUserIds by remember(members, selectAllMembersByDefaultForExpenses) {
        mutableStateOf(
            if (selectAllMembersByDefaultForExpenses) allMemberIds else emptyList(),
        )
    }
    var inputByUserId by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val backendDateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US) }
    val uiDateFormatter = remember { DateTimeFormatter.ofPattern("dd-MMM", Locale.US) }
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var paymentDate by remember { mutableStateOf(today) }

    val defaultValueProvider: (String) -> String = { _ ->
        when (splitType) {
            SplitType.EQUAL -> ""
            SplitType.EXACT -> ""
            SplitType.PERCENTAGE -> {
                if (participantUserIds.isEmpty()) {
                    ""
                } else {
                    "%.2f".format(Locale.US, 100.0 / participantUserIds.size.toDouble())
                }
            }

            SplitType.CUSTOM -> "1"
        }
    }

    val splitLabel = when (splitType) {
        SplitType.EQUAL -> ""
        SplitType.EXACT -> "Exact Amount"
        SplitType.PERCENTAGE -> "Percentage (%)"
        SplitType.CUSTOM -> "Custom Units"
    }
    val categoryOptions = remember { ExpenseCategory.entries.toList() }
    val splitTypeOptions = remember { SplitType.entries.toList() }
    val splitTypeTabs = remember {
        listOf("Equal", "Exact", "Percent", "Custom")
    }
    val selectedSplitTypeIndex = splitTypeOptions.indexOf(splitType).coerceAtLeast(0)
    val displayNameFor: (ExpenseMemberOption) -> String = { member ->
        member.displayName.ifBlank { "Member" }.take(16)
    }
    val selectedParticipantSummary = when {
        participantUserIds.isEmpty() -> "No members selected"
        participantUserIds.size == allMemberIds.size -> "All Members"
        else -> {
            val selectedNames = members
                .filter { it.userId in participantUserIds }
                .map(displayNameFor)
            selectedNames.joinToString(", ").let { names ->
                if (names.length <= 36) names else "${selectedNames.size} members selected"
            }
        }
    }
    val canSave = title.isNotBlank() &&
        amount.isNotBlank() &&
        paidByUserId.isNotBlank() &&
        participantUserIds.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Start with the amount, then fill in the rest of the details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 4.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Total") },
                            prefix = {
                                Text(
                                    text = "\u20B9",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineSmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Text(
                            text = "Enter the full expense total in INR.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categoryOptions.forEach { option ->
                            FilterChip(
                                selected = option == category,
                                onClick = { category = option },
                                label = { Text(option.displayLabel) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = paymentDate.format(uiDateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Date") },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                val picker = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                        if (!selected.isAfter(today)) {
                                            paymentDate = selected
                                        }
                                    },
                                    paymentDate.year,
                                    paymentDate.monthValue - 1,
                                    paymentDate.dayOfMonth,
                                )
                                picker.datePicker.maxDate = System.currentTimeMillis()
                                picker.show()
                            },
                        ) {
                            Text("Select")
                        }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { updated -> note = updated.take(140) },
                    label = { Text("Note (Optional)") },
                    supportingText = {
                        Text("${note.length}/140")
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paid By",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        members.forEach { member ->
                            MemberSelectionChip(
                                displayName = displayNameFor(member),
                                photoUrl = member.photoUrl,
                                selected = member.userId == paidByUserId,
                                onClick = { paidByUserId = member.userId },
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Split Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    PillTabSelector(
                        tabs = splitTypeTabs,
                        selectedIndex = selectedSplitTypeIndex,
                        onSelectedIndexChange = { index ->
                            splitType = splitTypeOptions[index]
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val allSelected = participantUserIds.size == allMemberIds.size && allMemberIds.isNotEmpty()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Participants",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = selectedParticipantSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = allSelected,
                            onClick = {
                                participantUserIds = if (allSelected) {
                                    emptyList()
                                } else {
                                    allMemberIds
                                }
                            },
                            label = { Text(if (allSelected) "Clear All" else "All Members") },
                        )
                        members.forEach { member ->
                            val selected = member.userId in participantUserIds
                            MemberSelectionChip(
                                displayName = displayNameFor(member),
                                photoUrl = member.photoUrl,
                                selected = selected,
                                onClick = {
                                    participantUserIds = if (selected) {
                                        participantUserIds.filterNot { it == member.userId }
                                    } else {
                                        (participantUserIds + member.userId).distinct()
                                    }
                                },
                            )
                        }
                    }
                }
                if (splitType != SplitType.EQUAL) {
                    Text(splitLabel, style = MaterialTheme.typography.labelLarge)
                    participantUserIds.forEach { memberId ->
                        val member = members.firstOrNull { it.userId == memberId }
                        val value = inputByUserId[memberId] ?: defaultValueProvider(memberId)
                        OutlinedTextField(
                            value = value,
                            onValueChange = { updated ->
                                inputByUserId = inputByUserId + (memberId to updated)
                            },
                            label = { Text(member?.displayName?.ifBlank { "Member" } ?: "Member") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                            ),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                GradientButton(
                    text = "Save",
                    onClick = {
                        val selectedParticipants = participantUserIds.distinct()
                        val splitInputs = if (splitType == SplitType.EQUAL) {
                            emptyList()
                        } else {
                            selectedParticipants.map { memberId ->
                                val value = inputByUserId[memberId] ?: defaultValueProvider(memberId)
                                SplitInputDraft(userId = memberId, value = value.trim())
                            }
                        }
                        onSave(
                            ExpenseDraft(
                                title = title.trim(),
                                amountRupees = amount.trim(),
                                paymentDate = paymentDate.format(backendDateFormatter),
                                category = category,
                                note = note.trim(),
                                splitType = splitType,
                                paidByUserId = paidByUserId,
                                participantUserIds = selectedParticipants,
                                splitInputs = splitInputs,
                            ),
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MemberSelectionChip(
    displayName: String,
    photoUrl: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(displayName) },
        leadingIcon = {
            MemberAvatar(
                displayName = displayName,
                photoUrl = photoUrl,
            )
        },
    )
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
                .size(20.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
        Box(
            modifier = modifier
                .size(20.dp)
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
