package com.buddingintents.letsgodutch.core.data.repository.firebase

import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import com.google.firebase.database.DataSnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun DataSnapshot.toGroupOrNull(): Group? {
    val id = key ?: return null
    val meta = child("meta")
    val invite = child("invite")

    val name = childString("name")
        .ifBlank { meta.childString("name") }
    val ownerUserId = childString("ownerUserId")
        .ifBlank { meta.childString("ownerUserId") }
    if (name.isBlank() || ownerUserId.isBlank()) return null

    val currencyCode = childString("currencyCode")
        .ifBlank { meta.childString("currencyCode") }
        .ifBlank { "INR" }
    val maxMembers = childLongNullable("maxMembers")
        ?: meta.childLongNullable("maxMembers")
        ?: 50L
    val createdAtEpochMs = childLongNullable("createdAtEpochMs")
        ?: meta.childLongNullable("createdAtEpochMs")
        ?: meta.childLongNullable("createdAt")
        ?: 0L
    val description = childString("description")
        .ifBlank { meta.childString("description") }
    val inviteCode = childString("inviteCode")
        .ifBlank { invite.childString("code") }
    val inviteExpiryEpochMs = childLongNullable("inviteExpiryEpochMs")
        ?: invite.childLongNullable("expiresAt")
        ?: 0L
    val autoRenewInvite = child("autoRenewInvite").getValue(Boolean::class.java)
        ?: invite.child("autoRenew").getValue(Boolean::class.java)
        ?: true
    val selectAllMembersByDefaultForExpenses = child("selectAllMembersByDefaultForExpenses")
        .getValue(Boolean::class.java)
        ?: child("expenseDefaultSelectAll").getValue(Boolean::class.java)
        ?: false
    val active = child("active").getValue(Boolean::class.java)
        ?: meta.child("active").getValue(Boolean::class.java)
        ?: true

    return Group(
        groupId = id,
        name = name,
        ownerUserId = ownerUserId,
        currencyCode = currencyCode,
        maxMembers = maxMembers.toInt().takeIf { it > 0 } ?: 50,
        createdAtEpochMs = createdAtEpochMs,
        description = description,
        inviteCode = inviteCode,
        inviteExpiryEpochMs = inviteExpiryEpochMs,
        autoRenewInvite = autoRenewInvite,
        selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
        active = active,
    )
}

internal fun DataSnapshot.toMemberOrNull(): Member? {
    val userId = key ?: return null
    val email = childString("email")
    val displayName = childString("displayName")
    val identifier = childString("identifier")
        .ifBlank { childString("deviceId") }
    val photoUrl = childString("photoUrl").ifBlank { null }
    val resolvedEmail = email.ifBlank { "$userId@example.com" }
    val resolvedDisplayName = displayName.ifBlank {
        email.substringBefore("@").takeIf { it.isNotBlank() } ?: userId
    }
    return Member(
        userId = userId,
        displayName = resolvedDisplayName.ifBlank { "Member" },
        email = resolvedEmail,
        identifier = identifier,
        photoUrl = photoUrl,
        joinedAtEpochMs = childLongNullable("joinedAtEpochMs")
            ?: childLongNullable("joinedAt")
            ?: 0L,
        role = roleFromValue(childString("role")),
        active = childBool("active", default = true),
    )
}

internal fun DataSnapshot.toExpenseOrNull(): Expense? {
    val expenseId = key ?: return null
    val groupId = childString("groupId")
    val title = childString("title")
    val paidBy = childString("paidByUserId")
    val createdBy = childString("createdByUserId")
    if (groupId.isBlank() || title.isBlank() || paidBy.isBlank() || createdBy.isBlank()) return null

    val participantUserIds = child("participantUserIds").children
        .mapNotNull { it.getValue(String::class.java) }
        .distinct()
    if (participantUserIds.isEmpty()) return null

    val splitType = splitTypeFromValue(childString("splitType"))
    val shares = child("shares").children.mapNotNull { shareSnapshot ->
        val userId = shareSnapshot.key ?: return@mapNotNull null
        SplitShare(
            userId = userId,
            amountPaise = shareSnapshot.childLongNullable("amountPaise"),
            percentage = shareSnapshot.childDoubleNullable("percentage"),
            customUnits = shareSnapshot.childDoubleNullable("customUnits"),
        )
    }

    return Expense(
        expenseId = expenseId,
        groupId = groupId,
        title = title,
        amountPaise = childLong("amountPaise"),
        paymentDate = childString("paymentDate")
            .takeIf { it.isNotBlank() && it.isBackendPaymentDateFormat() }
            ?: childLongNullable("createdAtEpochMs").toBackendPaymentDateOrToday(),
        currencyCode = childString("currencyCode").ifBlank { "INR" },
        paidByUserId = paidBy,
        participantUserIds = participantUserIds,
        splitType = splitType,
        shares = shares,
        createdByUserId = createdBy,
        createdAtEpochMs = childLong("createdAtEpochMs"),
        updatedAtEpochMs = childLong("updatedAtEpochMs"),
    )
}

internal fun DataSnapshot.toBalanceOrNull(): Balance? {
    val userId = key ?: return null
    return Balance(
        userId = userId,
        netPaise = childLong("netPaise"),
    )
}

internal fun Group.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "ownerUserId" to ownerUserId,
        "currencyCode" to currencyCode,
        "maxMembers" to maxMembers,
        "createdAtEpochMs" to createdAtEpochMs,
        "description" to description,
        "inviteCode" to inviteCode,
        "inviteExpiryEpochMs" to inviteExpiryEpochMs,
        "autoRenewInvite" to autoRenewInvite,
        "selectAllMembersByDefaultForExpenses" to selectAllMembersByDefaultForExpenses,
        "active" to active,
    )
}

internal fun Member.toFirebaseMap(): Map<String, Any> {
    val payload = mutableMapOf<String, Any>(
        "displayName" to displayName,
        "email" to email,
        "joinedAtEpochMs" to joinedAtEpochMs,
        "role" to role.name,
        "active" to active,
    )
    identifier.takeIf { it.isNotBlank() }?.let { payload["identifier"] = it }
    photoUrl?.takeIf { it.isNotBlank() }?.let { payload["photoUrl"] = it }
    return payload
}

internal fun Expense.toFirebaseMap(): Map<String, Any> {
    val sharesMap = shares.associate { share ->
        val payload = mutableMapOf<String, Any>()
        share.amountPaise?.let { payload["amountPaise"] = it }
        share.percentage?.let { payload["percentage"] = it }
        share.customUnits?.let { payload["customUnits"] = it }
        share.userId to payload
    }
    return mapOf(
        "groupId" to groupId,
        "title" to title,
        "amountPaise" to amountPaise,
        "paymentDate" to paymentDate.ifBlank { createdAtEpochMs.toBackendPaymentDateOrToday() },
        "currencyCode" to currencyCode,
        "paidByUserId" to paidByUserId,
        "participantUserIds" to participantUserIds,
        "splitType" to splitType.name,
        "shares" to sharesMap,
        "createdByUserId" to createdByUserId,
        "createdAtEpochMs" to createdAtEpochMs,
        "updatedAtEpochMs" to updatedAtEpochMs,
    )
}

internal fun Balance.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "netPaise" to netPaise,
    )
}

internal fun DataSnapshot.childString(key: String): String {
    return child(key).getValue(String::class.java).orEmpty()
}

internal fun DataSnapshot.childLong(key: String): Long {
    return childLongNullable(key) ?: 0L
}

internal fun DataSnapshot.childLongNullable(key: String): Long? {
    return child(key).getValue(Long::class.java)
        ?: child(key).getValue(Int::class.java)?.toLong()
}

internal fun DataSnapshot.childDoubleNullable(key: String): Double? {
    return child(key).getValue(Double::class.java)
        ?: child(key).getValue(Long::class.java)?.toDouble()
        ?: child(key).getValue(Int::class.java)?.toDouble()
}

internal fun DataSnapshot.childBool(key: String, default: Boolean): Boolean {
    return child(key).getValue(Boolean::class.java) ?: default
}

private fun splitTypeFromValue(value: String): SplitType {
    return SplitType.entries.firstOrNull { it.name == value } ?: SplitType.EQUAL
}

private fun roleFromValue(value: String): Role {
    return Role.entries.firstOrNull { it.name == value } ?: Role.MEMBER
}

private val backendPaymentDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)

private fun String.isBackendPaymentDateFormat(): Boolean {
    return runCatching {
        LocalDate.parse(this, backendPaymentDateFormatter)
    }.isSuccess
}

private fun Long?.toBackendPaymentDateOrToday(): String {
    val date = if (this != null && this > 0L) {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } else {
        LocalDate.now()
    }
    return date.format(backendPaymentDateFormatter)
}
