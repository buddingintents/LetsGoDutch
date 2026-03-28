package com.buddingintents.letsgodutch.feature.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val splitType: SplitType,
    val paidByUserId: String,
    val participantUserIds: List<String>,
    val splitInputs: List<SplitInputDraft>,
)

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
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var paidByExpanded by remember { mutableStateOf(false) }
    var splitTypeExpanded by remember { mutableStateOf(false) }
    var paidByFieldWidthPx by remember { mutableStateOf(0) }
    var splitTypeFieldWidthPx by remember { mutableStateOf(0) }
    var participantsExpanded by remember { mutableStateOf(false) }
    var participantsFieldWidthPx by remember { mutableStateOf(0) }
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
    val density = LocalDensity.current
    val backendDateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US) }
    val uiDateFormatter = remember { DateTimeFormatter.ofPattern("dd-MMM", Locale.US) }
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("Add Expense") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("\u20B9") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
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
                Box {
                    OutlinedTextField(
                        value = members.firstOrNull { it.userId == paidByUserId }?.displayName
                            ?.ifBlank { "Member" }
                            .orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid By") },
                        trailingIcon = {
                            TextButton(onClick = { paidByExpanded = !paidByExpanded }) {
                                Text("Select")
                            }
                        },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                paidByFieldWidthPx = coordinates.size.width
                            },
                        singleLine = true,
                    )
                    DropdownMenu(
                        expanded = paidByExpanded,
                        onDismissRequest = { paidByExpanded = false },
                        modifier = if (paidByFieldWidthPx > 0) {
                            Modifier.width(with(density) { paidByFieldWidthPx.toDp() })
                        } else {
                            Modifier
                        },
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(displayNameFor(member)) },
                                leadingIcon = {
                                    MemberAvatar(
                                        displayName = displayNameFor(member),
                                        photoUrl = member.photoUrl,
                                    )
                                },
                                onClick = {
                                    paidByUserId = member.userId
                                    paidByExpanded = false
                                },
                            )
                        }
                    }
                }

                Box {
                    OutlinedTextField(
                        value = splitType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Split Type") },
                        trailingIcon = {
                            TextButton(onClick = { splitTypeExpanded = !splitTypeExpanded }) {
                                Text("Select")
                            }
                        },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                splitTypeFieldWidthPx = coordinates.size.width
                            },
                        singleLine = true,
                    )
                    DropdownMenu(
                        expanded = splitTypeExpanded,
                        onDismissRequest = { splitTypeExpanded = false },
                        modifier = if (splitTypeFieldWidthPx > 0) {
                            Modifier.width(with(density) { splitTypeFieldWidthPx.toDp() })
                        } else {
                            Modifier
                        },
                    ) {
                        SplitType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    splitType = type
                                    splitTypeExpanded = false
                                },
                            )
                        }
                    }
                }

                Box {
                    val allSelected = participantUserIds.size == allMemberIds.size && allMemberIds.isNotEmpty()
                    OutlinedTextField(
                        value = selectedParticipantSummary,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Participants") },
                        trailingIcon = {
                            TextButton(onClick = { participantsExpanded = !participantsExpanded }) {
                                Text("Select")
                            }
                        },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                participantsFieldWidthPx = coordinates.size.width
                            },
                    )
                    DropdownMenu(
                        expanded = participantsExpanded,
                        onDismissRequest = { participantsExpanded = false },
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .then(
                                if (participantsFieldWidthPx > 0) {
                                    Modifier.width(with(density) { participantsFieldWidthPx.toDp() })
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (allSelected) "Clear All" else "All Members") },
                            trailingIcon = {
                                Checkbox(
                                    checked = allSelected,
                                    onCheckedChange = null,
                                )
                            },
                            onClick = {
                                participantUserIds = if (allSelected) {
                                    emptyList()
                                } else {
                                    allMemberIds
                                }
                            },
                        )
                        members.forEach { member ->
                            val selected = member.userId in participantUserIds
                            DropdownMenuItem(
                                text = { Text(displayNameFor(member)) },
                                leadingIcon = {
                                    MemberAvatar(
                                        displayName = displayNameFor(member),
                                        photoUrl = member.photoUrl,
                                    )
                                },
                                trailingIcon = {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = null,
                                    )
                                },
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
        },
        confirmButton = {
            Button(
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
                            splitType = splitType,
                            paidByUserId = paidByUserId,
                            participantUserIds = selectedParticipants,
                            splitInputs = splitInputs,
                        ),
                    )
                },
                enabled = title.isNotBlank() &&
                    amount.isNotBlank() &&
                    paidByUserId.isNotBlank() &&
                    participantUserIds.isNotEmpty(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
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
