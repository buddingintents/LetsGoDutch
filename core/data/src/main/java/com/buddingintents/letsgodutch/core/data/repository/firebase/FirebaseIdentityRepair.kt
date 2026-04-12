package com.buddingintents.letsgodutch.core.data.repository.firebase

import com.buddingintents.letsgodutch.core.data.repository.mergeMemberBalanceIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeUserIdReferences
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

internal suspend fun collectSettlementActivityMergeUpdates(
    root: DatabaseReference,
    groupId: String,
    fromUserId: String,
    toUserId: String,
    mergedDisplayName: String = "",
): Map<String, Any?> {
    if (groupId.isBlank() || fromUserId.isBlank() || toUserId.isBlank() || fromUserId == toUserId) {
        return emptyMap()
    }

    return root.child("settlementActivities").child(groupId).get().await().children
        .mapNotNull { snapshot ->
            val activity = snapshot.toSettlementUpiTransactionOrNull() ?: return@mapNotNull null
            val mergedActivity = activity.mergeMemberIntoUser(
                fromUserId = fromUserId,
                toUserId = toUserId,
                mergedDisplayName = mergedDisplayName,
            )
            if (mergedActivity == activity) {
                null
            } else {
                "settlementActivities/$groupId/${activity.activityId}" to
                    mergedActivity.copy(activityId = activity.activityId).toFirebaseMap()
            }
        }
        .toMap(linkedMapOf())
}

internal suspend fun repairGroupOwnerMembershipAndReferences(
    root: DatabaseReference,
    groupId: String,
) {
    if (groupId.isBlank()) return

    val group = root.child("groups").child(groupId).get().await().toGroupOrNull() ?: return
    val ownerUserId = group.ownerUserId.trim()
    if (ownerUserId.isBlank()) return

    val members = root.child("groupMembers").child(groupId).get().await().children
        .mapNotNull { it.toMemberOrNull() }
        .filter { it.active }
    if (members.any { it.userId == ownerUserId }) {
        root.child("userGroups").child(ownerUserId).child(groupId).setValue(true).await()
        canonicalizeLegacyAliasReferences(
            root = root,
            groupId = groupId,
            activeMembers = members,
        )
        return
    }

    val ownerProfileSnapshot = root.child("users").child(ownerUserId).child("profile").get().await()
    val fallbackOwnerMember = members.singleOrNull { it.role == Role.OWNER && it.userId != ownerUserId }

    if (fallbackOwnerMember != null) {
        val mergedOwnerMember = buildOwnerRepairMember(
            ownerUserId = ownerUserId,
            ownerProfileSnapshot = ownerProfileSnapshot,
            fallbackOwnerMember = fallbackOwnerMember,
            fallbackJoinedAtEpochMs = group.createdAtEpochMs,
        )
        val updates = mutableMapOf<String, Any?>(
            "groupMembers/$groupId/$ownerUserId" to mergedOwnerMember.toFirebaseMap(),
            "groupMembers/$groupId/${fallbackOwnerMember.userId}" to null,
            "userGroups/$ownerUserId/$groupId" to true,
            "userGroups/${fallbackOwnerMember.userId}/$groupId" to null,
        )

        loadGroupExpenses(root = root, groupId = groupId).forEach { expense ->
            val mergedExpense = expense.mergeMemberIntoUser(
                fromUserId = fallbackOwnerMember.userId,
                toUserId = ownerUserId,
            )
            if (mergedExpense != expense) {
                updates["expenses/$groupId/${expense.expenseId}"] = mergedExpense.toFirebaseMap()
            }
        }

        val balances = root.child("balances").child(groupId).get().await().children
            .associateNotNull(
                keySelector = { it.key },
                valueSelector = { it.childLongNullable("netPaise") },
            )
        if (balances.containsKey(fallbackOwnerMember.userId)) {
            updates["balances/$groupId"] = balances
                .mergeMemberBalanceIntoUser(
                    fromUserId = fallbackOwnerMember.userId,
                    toUserId = ownerUserId,
                )
                .toBalancePayload()
        }

        val dispatchMembers = root.child("settlementDispatch").child(groupId).child("members").get().await().children
            .mapNotNull { it.getValue(String::class.java) }
        if (dispatchMembers.isNotEmpty()) {
            val mergedDispatchMembers = dispatchMembers.mergeUserIdReferences(
                fromUserId = fallbackOwnerMember.userId,
                toUserId = ownerUserId,
            )
            if (mergedDispatchMembers != dispatchMembers) {
                updates["settlementDispatch/$groupId/members"] = mergedDispatchMembers
                updates["settlementDispatch/$groupId/memberCount"] = mergedDispatchMembers.size
            }
        }

        updates.putAll(
            collectSettlementActivityMergeUpdates(
                root = root,
                groupId = groupId,
                fromUserId = fallbackOwnerMember.userId,
                toUserId = ownerUserId,
                mergedDisplayName = mergedOwnerMember.displayName,
            ),
        )

        root.updateChildren(updates).await()
        canonicalizeLegacyAliasReferences(
            root = root,
            groupId = groupId,
            activeMembers = members
                .filterNot { it.userId == fallbackOwnerMember.userId }
                .plus(mergedOwnerMember),
        )
        return
    }

    val repairedOwnerMember = buildOwnerRepairMember(
        ownerUserId = ownerUserId,
        ownerProfileSnapshot = ownerProfileSnapshot,
        fallbackOwnerMember = null,
        fallbackJoinedAtEpochMs = members.mapNotNull { it.joinedAtEpochMs.takeIf { joinedAt -> joinedAt > 0L } }
            .minOrNull()
            ?: group.createdAtEpochMs,
    )
    val updates = mutableMapOf<String, Any?>(
        "groupMembers/$groupId/$ownerUserId" to repairedOwnerMember.toFirebaseMap(),
        "userGroups/$ownerUserId/$groupId" to true,
    )
    root.updateChildren(updates).await()
    canonicalizeLegacyAliasReferences(
        root = root,
        groupId = groupId,
        activeMembers = members.plus(repairedOwnerMember),
    )
}

private suspend fun canonicalizeLegacyAliasReferences(
    root: DatabaseReference,
    groupId: String,
    activeMembers: List<Member>,
): Boolean {
    val legacyAliasToCanonicalUserId = buildLegacyAliasMap(activeMembers)
    if (legacyAliasToCanonicalUserId.isEmpty()) return false

    val memberById = activeMembers.associateBy { it.userId }
    var changed = false

    val expenses = loadGroupExpenses(root = root, groupId = groupId)
    val mergedExpenses = expenses.map { expense ->
        legacyAliasToCanonicalUserId.entries.fold(expense) { current, (legacyAliasUserId, canonicalUserId) ->
            current.mergeMemberIntoUser(
                fromUserId = legacyAliasUserId,
                toUserId = canonicalUserId,
            )
        }
    }
    if (mergedExpenses != expenses) {
        root.child("expenses").child(groupId).setValue(
            mergedExpenses.associate { expense ->
                expense.expenseId to expense.toFirebaseMap()
            },
        ).await()
        changed = true
    }

    val settlementActivities = loadSettlementActivities(root = root, groupId = groupId)
    val mergedSettlementActivities = settlementActivities.map { activity ->
        legacyAliasToCanonicalUserId.entries.fold(activity) { current, (legacyAliasUserId, canonicalUserId) ->
            current.mergeMemberIntoUser(
                fromUserId = legacyAliasUserId,
                toUserId = canonicalUserId,
                mergedDisplayName = memberById[canonicalUserId]?.displayName.orEmpty(),
            )
        }
    }
    if (mergedSettlementActivities != settlementActivities) {
        root.child("settlementActivities").child(groupId).setValue(
            mergedSettlementActivities.associate { activity ->
                activity.activityId to activity.copy(activityId = activity.activityId).toFirebaseMap()
            },
        ).await()
        changed = true
    }

    val dispatchRef = root.child("settlementDispatch").child(groupId)
    val dispatchMembers = dispatchRef.child("members").get().await().children
        .mapNotNull { it.getValue(String::class.java) }
    val mergedDispatchMembers = legacyAliasToCanonicalUserId.entries.fold(dispatchMembers) {
        currentMembers,
        (legacyAliasUserId, canonicalUserId),
        ->
        currentMembers.mergeUserIdReferences(
            fromUserId = legacyAliasUserId,
            toUserId = canonicalUserId,
        )
    }
    if (mergedDispatchMembers != dispatchMembers) {
        dispatchRef.child("members").setValue(mergedDispatchMembers).await()
        dispatchRef.child("memberCount").setValue(mergedDispatchMembers.size).await()
        changed = true
    }

    val dispatchUserUpdates = mutableMapOf<String, Any?>()
    legacyAliasToCanonicalUserId.forEach { (legacyAliasUserId, canonicalUserId) ->
        dispatchUserUpdates["userGroups/$canonicalUserId/$groupId"] = true
        dispatchUserUpdates["userGroups/$legacyAliasUserId/$groupId"] = null
    }
    if (dispatchUserUpdates.isNotEmpty()) {
        root.updateChildren(dispatchUserUpdates).await()
        changed = true
    }

    return changed
}

internal fun buildLegacyAliasMap(activeMembers: List<Member>): Map<String, String> {
    val activeMemberIds = activeMembers.map { it.userId }.filter { it.isNotBlank() }.toSet()
    if (activeMemberIds.isEmpty()) return emptyMap()

    return activeMembers.mapNotNull { member ->
        legacyPlaceholderAliasUserId(
            member = member,
            activeMemberIds = activeMemberIds,
        )?.let { legacyAliasUserId -> legacyAliasUserId to member.userId }
    }.toMap(linkedMapOf())
}

internal fun legacyPlaceholderAliasUserId(
    member: Member,
    activeMemberIds: Set<String>,
): String? {
    val rawEmail = member.email.trim()
    if (!rawEmail.endsWith("@example.com", ignoreCase = true)) return null
    val placeholderUserId = rawEmail.substringBefore("@").trim()
    if (placeholderUserId.isBlank() || placeholderUserId == member.userId) return null
    if (activeMemberIds.any { it.equals(placeholderUserId, ignoreCase = true) }) return null
    return placeholderUserId
}

private fun buildOwnerRepairMember(
    ownerUserId: String,
    ownerProfileSnapshot: DataSnapshot,
    fallbackOwnerMember: Member?,
    fallbackJoinedAtEpochMs: Long,
): Member {
    val isAnonymousUser = ownerProfileSnapshot.childBool("isAnonymous", default = false)
    val joinedAtEpochMs = fallbackOwnerMember?.joinedAtEpochMs
        ?.takeIf { it > 0L }
        ?: fallbackJoinedAtEpochMs.takeIf { it > 0L }
        ?: System.currentTimeMillis()
    val email = firstNonBlank(
        ownerProfileSnapshot.childString("email"),
        fallbackOwnerMember?.email.orEmpty(),
    ).ifBlank {
        if (isAnonymousUser) "" else "$ownerUserId@example.com"
    }

    return Member(
        userId = ownerUserId,
        displayName = firstNonBlank(
            ownerProfileSnapshot.childString("displayName"),
            fallbackOwnerMember?.displayName.orEmpty(),
            email.substringBefore("@").trim(),
            ownerUserId,
            "Member",
        ),
        email = email,
        identifier = firstNonBlank(
            ownerProfileSnapshot.childString("identifier"),
            ownerProfileSnapshot.childString("deviceId"),
            fallbackOwnerMember?.identifier.orEmpty(),
        ),
        photoUrl = firstNonBlank(
            ownerProfileSnapshot.childString("photoUrl"),
            fallbackOwnerMember?.photoUrl.orEmpty(),
        ).ifBlank { null },
        upiId = firstNonBlank(
            ownerProfileSnapshot.childString("upiId"),
            fallbackOwnerMember?.upiId.orEmpty(),
        ),
        joinedAtEpochMs = joinedAtEpochMs,
        role = Role.OWNER,
        active = true,
    )
}

private fun Map<String, Long>.toBalancePayload(): Map<String, Map<String, Any>> {
    return mapValues { (userId, netPaise) ->
        mapOf(
            "userId" to userId,
            "netPaise" to netPaise,
        )
    }
}

private inline fun <T : Any, R : Any> Iterable<T>.associateNotNull(
    keySelector: (T) -> String?,
    valueSelector: (T) -> R?,
): Map<String, R> {
    val map = linkedMapOf<String, R>()
    forEach { item ->
        val key = keySelector(item) ?: return@forEach
        val value = valueSelector(item) ?: return@forEach
        map[key] = value
    }
    return map
}

private fun firstNonBlank(vararg values: String): String {
    return values.firstOrNull { it.isNotBlank() }.orEmpty()
}
