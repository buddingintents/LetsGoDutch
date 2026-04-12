package com.buddingintents.letsgodutch.core.data.repository.firebase

import com.buddingintents.letsgodutch.core.model.GroupActivity
import com.buddingintents.letsgodutch.core.model.GroupActivityType
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

internal suspend fun appendGroupActivity(
    root: DatabaseReference,
    groupId: String,
    type: GroupActivityType,
    actorUserId: String,
    actorName: String,
    title: String,
    detail: String = "",
    createdAtEpochMs: Long = System.currentTimeMillis(),
) {
    if (groupId.isBlank() || title.isBlank()) return
    val activityId = root.child("activities").child(groupId).push().key
        ?: "activity_$createdAtEpochMs"
    val activity = GroupActivity(
        activityId = activityId,
        groupId = groupId,
        type = type,
        actorUserId = actorUserId,
        actorName = actorName,
        title = title,
        detail = detail,
        createdAtEpochMs = createdAtEpochMs,
    )
    root.child("activities")
        .child(groupId)
        .child(activityId)
        .setValue(activity.toFirebaseMap())
        .await()
}

internal suspend fun resolveActivityDisplayName(
    root: DatabaseReference,
    groupId: String,
    userId: String,
    fallback: String = "",
): String {
    if (userId.isBlank()) return fallback.ifBlank { "Member" }

    val memberSnapshot = runCatching {
        root.child("groupMembers").child(groupId).child(userId).get().await()
    }.getOrNull()
    val memberName = memberSnapshot?.childString("displayName").orEmpty().trim()
    if (memberName.isNotBlank() && !memberName.equals("member", ignoreCase = true)) {
        return memberName
    }

    val profileSnapshot = runCatching {
        root.child("users").child(userId).child("profile").get().await()
    }.getOrNull()
    val profileName = profileSnapshot?.childString("displayName").orEmpty().trim()
    if (profileName.isNotBlank() && !profileName.equals("member", ignoreCase = true)) {
        return profileName
    }

    val emailAlias = profileSnapshot?.childString("email").orEmpty().substringBefore("@").trim()
    if (emailAlias.isNotBlank() && !emailAlias.equals(userId, ignoreCase = true)) {
        return emailAlias
    }

    return fallback.ifBlank { "Member" }
}
