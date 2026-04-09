package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.GroupRepository
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberBalanceIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeUserIdReferences
import com.buddingintents.letsgodutch.core.model.ExitLiabilityChoice
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.JoinGroupPreview
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseGroupRepository(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : GroupRepository {

    private val root = database.reference

    override fun observeGroupsForUser(userId: String): Flow<List<Group>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val userGroupsRef = root.child("userGroups").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch {
                    runCatching {
                        val groupIds = snapshot.children.mapNotNull { it.key }.distinct()
                        if (groupIds.isEmpty()) {
                            trySend(emptyList())
                            return@runCatching
                        }
                        val groups = groupIds.mapNotNull { groupId ->
                            loadVisibleGroupForUser(
                                groupId = groupId,
                                userId = userId,
                            )
                        }
                            .sortedByDescending { it.createdAtEpochMs }
                        trySend(groups)
                    }.onFailure { throwable ->
                        Log.w("FirebaseGroupRepo", "observeGroupsForUser failed while loading groups.", throwable)
                        trySend(emptyList())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseGroupRepo",
                    "observeGroupsForUser cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }

        userGroupsRef.addValueEventListener(listener)
        awaitClose {
            userGroupsRef.removeEventListener(listener)
            scope.cancel()
        }
    }

    override fun observeMembers(groupId: String): Flow<List<Member>> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val membersRef = root.child("groupMembers").child(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch {
                    runCatching {
                        val members = snapshot.children.mapNotNull { it.toMemberOrNull() }
                            .filter { it.active }
                            .map { member -> enrichMemberFromProfileIfNeeded(member) }
                            .sortedBy { it.joinedAtEpochMs }
                        trySend(members)
                    }.onFailure { throwable ->
                        Log.w("FirebaseGroupRepo", "observeMembers failed while loading members.", throwable)
                        trySend(emptyList())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseGroupRepo",
                    "observeMembers cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }
        membersRef.addValueEventListener(listener)
        awaitClose {
            membersRef.removeEventListener(listener)
            scope.cancel()
        }
    }

    override suspend fun createGroup(
        name: String,
        ownerUserId: String,
        description: String,
        autoRenewInvite: Boolean,
        selectAllMembersByDefaultForExpenses: Boolean,
    ): Result<Group> {
        return runCatching {
            require(name.isNotBlank()) { "Group name is required." }
            require(ownerUserId.isNotBlank()) { "Owner user id is required." }

            val now = System.currentTimeMillis()
            val groupId = root.child("groups").push().key ?: error("Unable to allocate group id.")
            val group = Group(
                groupId = groupId,
                name = name.trim(),
                ownerUserId = ownerUserId,
                createdAtEpochMs = now,
                description = description.trim(),
                inviteCode = generateInviteCode(),
                inviteExpiryEpochMs = now + INVITE_EXPIRY_WEEK_MS,
                autoRenewInvite = autoRenewInvite,
                selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
            )
            val ownerMember = resolveMember(ownerUserId, now, role = Role.OWNER)

            try {
                root.child("groups").child(groupId).setValue(group.toFirebaseMap()).await()
                root.child("groupMembers").child(groupId).child(ownerUserId).setValue(ownerMember.toFirebaseMap()).await()
                root.child("userGroups").child(ownerUserId).child(groupId).setValue(true).await()
            } catch (error: Throwable) {
                runCatching { root.child("groupMembers").child(groupId).child(ownerUserId).removeValue().await() }
                runCatching { root.child("userGroups").child(ownerUserId).child(groupId).removeValue().await() }
                runCatching { root.child("groups").child(groupId).removeValue().await() }
                throw error
            }
            group
        }
    }

    override suspend fun previewJoinWithInvite(inviteCode: String, userId: String): Result<JoinGroupPreview> {
        return runCatching {
            require(inviteCode.isNotBlank()) { "Invite code is required." }
            require(userId.isNotBlank()) { "User id is required." }

            val inviteTarget = resolveInviteTarget(inviteCode = inviteCode)
            val group = inviteTarget.group
            val existingMembers = loadActiveMembers(group.groupId)
            val identityKeys = loadUserIdentityKeys(userId)
            val alreadyJoined = existingMembers.any { member ->
                member.matchesIdentityKeys(identityKeys)
            }
            val claimableMembers = if (alreadyJoined) {
                emptyList()
            } else {
                existingMembers
                    .filter { isManualMemberUserId(it.userId) }
                    .sortedWith(
                        compareByDescending<Member> { it.role == Role.OWNER }
                            .thenBy { it.joinedAtEpochMs },
                    )
            }

            if (!alreadyJoined && existingMembers.size >= group.maxMembers && claimableMembers.isEmpty()) {
                error("Group is full.")
            }

            JoinGroupPreview(
                group = group,
                alreadyJoined = alreadyJoined,
                claimableMembers = claimableMembers,
            )
        }
    }

    override suspend fun joinGroupWithInvite(
        inviteCode: String,
        userId: String,
        claimMemberUserId: String?,
    ): Result<Group> {
        return runCatching {
            require(inviteCode.isNotBlank()) { "Invite code is required." }
            require(userId.isNotBlank()) { "User id is required." }

            val inviteTarget = resolveInviteTarget(inviteCode = inviteCode)
            val group = inviteTarget.group
            val existingMembers = loadActiveMembers(group.groupId)
            val existingActualMember = existingMembers.firstOrNull { it.userId == userId }

            if (claimMemberUserId.isNullOrBlank()) {
                if (existingActualMember == null) {
                    check(existingMembers.size < group.maxMembers) { "Group is full." }
                    val member = resolveMember(userId, System.currentTimeMillis(), role = Role.MEMBER)
                    try {
                        root.child("groupMembers").child(group.groupId).child(userId).setValue(member.toFirebaseMap()).await()
                        root.child("userGroups").child(userId).child(group.groupId).setValue(true).await()
                        runCatching {
                            enqueueMembershipNotification(
                                group = group,
                                actorUserId = userId,
                                type = "MEMBER_ADDED",
                                title = "New member joined",
                                body = "${member.displayName.ifBlank { "A member" }} joined ${group.name}.",
                                targetUserId = userId,
                            )
                        }
                    } catch (error: Throwable) {
                        runCatching { root.child("groupMembers").child(group.groupId).child(userId).removeValue().await() }
                        runCatching { root.child("userGroups").child(userId).child(group.groupId).removeValue().await() }
                        throw error
                    }
                }
                return@runCatching group
            }

            val mergedGroup = mergeClaimedMemberIntoUser(
                group = group,
                actualUserId = userId,
                claimMemberUserId = claimMemberUserId,
                existingMembers = existingMembers,
                existingActualMember = existingActualMember,
            )

            val mergedMember = resolveMember(
                userId = userId,
                joinedAt = minOf(
                    existingActualMember?.joinedAtEpochMs ?: Long.MAX_VALUE,
                    existingMembers.firstOrNull { it.userId == claimMemberUserId }?.joinedAtEpochMs ?: Long.MAX_VALUE,
                ).takeUnless { it == Long.MAX_VALUE } ?: System.currentTimeMillis(),
                role = if (mergedGroup.ownerUserId == userId) Role.OWNER else Role.MEMBER,
            )
            runCatching {
                enqueueMembershipNotification(
                    group = mergedGroup,
                    actorUserId = userId,
                    type = "MEMBER_ADDED",
                    title = "New member joined",
                    body = "${mergedMember.displayName.ifBlank { "A member" }} joined ${mergedGroup.name}.",
                    targetUserId = userId,
                )
            }

            mergedGroup
        }
    }

    override suspend fun updateGroupDetails(
        groupId: String,
        description: String,
        autoRenewInvite: Boolean,
        selectAllMembersByDefaultForExpenses: Boolean,
        actorUserId: String,
    ): Result<Group> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            check(group.active) { "Group is not active." }
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can update group details.",
            )

            val updatedGroup = group.copy(
                description = description.trim(),
                autoRenewInvite = autoRenewInvite,
                selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
            )
            root.child("groups").child(groupId).updateChildren(updatedGroup.toFirebaseMap()).await()
            updatedGroup
        }
    }

    override suspend fun renewInvite(groupId: String, actorUserId: String): Result<Group> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            check(group.active) { "Group is not active." }
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can renew the invite.",
            )

            renewInviteInternal(group)
        }
    }

    override suspend fun addManualMember(
        groupId: String,
        displayName: String,
        actorUserId: String,
    ): Result<Member> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Member name is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            check(group.active) { "Group is not active." }

            val membersRef = root.child("groupMembers").child(groupId)
            val membersSnapshot = membersRef.get().await()
            val existingMembers = membersSnapshot.children.mapNotNull { it.toMemberOrNull() }.filter { it.active }
            check(existingMembers.any { it.userId == actorUserId }) { "Only active group members can add members." }
            check(existingMembers.size < group.maxMembers) { "Group is full." }
            check(existingMembers.none { it.displayName.trim().equals(normalizedName, ignoreCase = true) }) {
                "A member with this name already exists."
            }

            val suffix = membersRef.push().key ?: System.currentTimeMillis().toString()
            val memberUserId = "guest_$suffix"
            val now = System.currentTimeMillis()
            val member = Member(
                userId = memberUserId,
                displayName = normalizedName,
                email = "",
                joinedAtEpochMs = now,
                role = Role.MEMBER,
                active = true,
            )
            membersRef.child(memberUserId).setValue(member.toFirebaseMap()).await()

            runCatching {
                enqueueMembershipNotification(
                    group = group,
                    actorUserId = actorUserId,
                    type = "MEMBER_ADDED",
                    title = "Member added",
                    body = "$normalizedName was added to ${group.name}.",
                    targetUserId = memberUserId,
                )
            }
            member
        }
    }

    override suspend fun updateMemberDisplayName(
        groupId: String,
        memberUserId: String,
        displayName: String,
        actorUserId: String,
    ): Result<Member> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(memberUserId.isNotBlank()) { "Member id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Member name is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            check(group.active) { "Group is not active." }
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can edit member names.",
            )
            check(memberUserId != group.ownerUserId) { "Main owner cannot be edited." }
            check(isManualMemberUserId(memberUserId)) { "Only manually added members can be edited." }

            val membersRef = root.child("groupMembers").child(groupId)
            val membersSnapshot = membersRef.get().await()
            val existingMembers = membersSnapshot.children.mapNotNull { it.toMemberOrNull() }.filter { it.active }
            val target = existingMembers.firstOrNull { it.userId == memberUserId }
                ?: error("Member not found.")
            check(existingMembers.none {
                it.userId != memberUserId && it.displayName.trim().equals(normalizedName, ignoreCase = true)
            }) {
                "A member with this name already exists."
            }

            membersRef.child(memberUserId).child("displayName").setValue(normalizedName).await()
            val updated = target.copy(displayName = normalizedName)

            runCatching {
                enqueueMembershipNotification(
                    group = group,
                    actorUserId = actorUserId,
                    type = "MEMBER_UPDATED",
                    title = "Member updated",
                    body = "$normalizedName was updated in ${group.name}.",
                    targetUserId = memberUserId,
                )
            }
            updated
        }
    }

    override suspend fun updateMemberRole(
        groupId: String,
        memberUserId: String,
        role: Role,
        actorUserId: String,
    ): Result<Member> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(memberUserId.isNotBlank()) { "Member id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            check(group.active) { "Group is not active." }
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can manage owner roles.",
            )
            check(memberUserId != group.ownerUserId) { "Main owner role cannot be changed." }

            val membersRef = root.child("groupMembers").child(groupId)
            val membersSnapshot = membersRef.get().await()
            val existingMembers = membersSnapshot.children.mapNotNull { it.toMemberOrNull() }.filter { it.active }
            val target = existingMembers.firstOrNull { it.userId == memberUserId }
                ?: error("Member not found.")
            if (target.role == role) return@runCatching target

            membersRef.child(memberUserId).child("role").setValue(role.name).await()
            val updated = target.copy(role = role)

            runCatching {
                val roleLabel = if (role == Role.OWNER) "owner" else "member"
                enqueueMembershipNotification(
                    group = group,
                    actorUserId = actorUserId,
                    type = "MEMBER_ROLE_UPDATED",
                    title = "Member role updated",
                    body = "${updated.displayName.ifBlank { "A member" }} is now $roleLabel in ${group.name}.",
                    targetUserId = memberUserId,
                )
            }
            updated
        }
    }

    override suspend fun removeMember(
        groupId: String,
        memberUserId: String,
        actorUserId: String,
        liabilityChoice: ExitLiabilityChoice,
    ): Result<Unit> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(memberUserId.isNotBlank()) { "Member id is required." }
            require(actorUserId.isNotBlank()) { "Actor user id is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can remove members.",
            )
            check(memberUserId != group.ownerUserId) { "Main owner cannot be removed from group." }

            leaveGroup(
                groupId = groupId,
                userId = memberUserId,
                liabilityChoice = liabilityChoice,
            ).getOrThrow()

            runCatching {
                enqueueMembershipNotification(
                    group = group,
                    actorUserId = actorUserId,
                    type = "MEMBER_REMOVED",
                    title = "Member removed",
                    body = "A member was removed from ${group.name}.",
                    targetUserId = memberUserId,
                )
            }
        }
    }

    override suspend fun leaveGroup(
        groupId: String,
        userId: String,
        liabilityChoice: ExitLiabilityChoice,
    ): Result<Unit> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(userId.isNotBlank()) { "User id is required." }

            val groupSnapshot = root.child("groups").child(groupId).get().await()
            val group = groupSnapshot.toGroupOrNull() ?: error("Group not found.")

            val membersSnapshot = root.child("groupMembers").child(groupId).get().await()
            val activeMembers = membersSnapshot.children.mapNotNull { it.toMemberOrNull() }.filter { it.active }
            if (activeMembers.none { it.userId == userId }) return@runCatching
            val remainingMembers = activeMembers.filterNot { it.userId == userId }

            val balancesSnapshot = root.child("balances").child(groupId).get().await()
            val balanceMap = balancesSnapshot.children.associateNotNull(
                keySelector = { it.key },
                valueSelector = { it.childLongNullable("netPaise") },
            ).toMutableMap()

            val leavingBalance = balanceMap[userId] ?: 0L
            if (remainingMembers.isNotEmpty() && leavingBalance != 0L) {
                when (liabilityChoice) {
                    ExitLiabilityChoice.DISTRIBUTE_EQUAL_TO_ACTIVE_MEMBERS -> {
                        val deltas = distributeAmountEvenly(
                            totalAmount = leavingBalance,
                            count = remainingMembers.size,
                        )
                        remainingMembers.forEachIndexed { index, member ->
                            balanceMap[member.userId] = (balanceMap[member.userId] ?: 0L) + deltas[index]
                        }
                    }

                    ExitLiabilityChoice.ABSORB_BY_OWNER -> {
                        val absorbTarget = if (group.ownerUserId == userId) {
                            remainingMembers.minByOrNull { it.joinedAtEpochMs }?.userId
                        } else {
                            group.ownerUserId
                        }
                        if (!absorbTarget.isNullOrBlank()) {
                            balanceMap[absorbTarget] = (balanceMap[absorbTarget] ?: 0L) + leavingBalance
                        }
                    }
                }
            }
            balanceMap.remove(userId)

            val updates = mutableMapOf<String, Any?>(
                "groupMembers/$groupId/$userId" to null,
                "userGroups/$userId/$groupId" to null,
            )

            if (userId == group.ownerUserId && remainingMembers.isNotEmpty()) {
                val nextMainOwner = remainingMembers
                    .filter { it.role == Role.OWNER }
                    .minByOrNull { it.joinedAtEpochMs }
                    ?: remainingMembers.minByOrNull { it.joinedAtEpochMs }
                if (nextMainOwner != null) {
                    updates["groups/$groupId/ownerUserId"] = nextMainOwner.userId
                    if (nextMainOwner.role != Role.OWNER) {
                        updates["groupMembers/$groupId/${nextMainOwner.userId}/role"] = Role.OWNER.name
                    }
                }
            }

            root.updateChildren(updates).await()
            persistBalances(groupId = groupId, balances = balanceMap).await()
        }
    }

    override suspend fun deleteGroup(groupId: String, actorUserId: String): Result<Unit> {
        return runCatching {
            require(groupId.isNotBlank()) { "Group id is required." }
            require(actorUserId.isNotBlank()) { "User id is required." }

            val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
                ?: error("Group not found.")
            ensureOwnerAccess(
                groupId = groupId,
                actorUserId = actorUserId,
                message = "Only an owner can delete the group.",
            )

            val memberIds = root.child("groupMembers").child(groupId).get().await().children
                .mapNotNull { it.key }
                .distinct()

            val updates = mutableMapOf<String, Any?>(
                "groups/$groupId" to null,
                "groupMembers/$groupId" to null,
                "expenses/$groupId" to null,
                "balances/$groupId" to null,
                "settlementDispatch/$groupId" to null,
            )
            memberIds.forEach { memberId ->
                updates["userGroups/$memberId/$groupId"] = null
            }

            root.updateChildren(updates).await()
        }
    }

    private suspend fun enqueueMembershipNotification(
        group: Group,
        actorUserId: String,
        type: String,
        title: String,
        body: String,
        targetUserId: String,
    ) {
        val members = root.child("groupMembers").child(group.groupId).get().await().children
            .mapNotNull { snapshot ->
                if (snapshot.childBool("active", default = true)) snapshot.key else null
            }
            .filterNot(::isManualMemberUserId)
            .distinct()
        if (members.isEmpty()) return

        val now = System.currentTimeMillis()
        val updates = mutableMapOf<String, Any?>()
        members.forEach { memberId ->
            val notificationId = root.child("notifications").child(memberId).push().key
                ?: "n_${now}_$memberId"
            updates["notifications/$memberId/$notificationId"] = mapOf(
                "type" to type,
                "groupId" to group.groupId,
                "title" to title,
                "body" to body,
                "byUserId" to actorUserId,
                "targetUserId" to targetUserId,
                "read" to false,
                "createdAtEpochMs" to now,
            )
        }
        if (updates.isNotEmpty()) {
            root.updateChildren(updates).await()
        }
    }

    private suspend fun resolveMember(userId: String, joinedAt: Long, role: Role): Member {
        val profileSnapshot = runCatching {
            root.child("users").child(userId).child("profile").get().await()
        }.getOrNull()
        val authUser = auth.currentUser?.takeIf { it.uid == userId }
        val isAnonymousUser = authUser?.isAnonymous == true ||
            (profileSnapshot?.childBool("isAnonymous", default = false) == true)

        val authEmail = authUser?.email.orEmpty().trim()
        val profileEmail = profileSnapshot?.childString("email").orEmpty().trim()
        val email = firstNotBlank(authEmail, profileEmail).ifBlank {
            if (isAnonymousUser) "" else "$userId@example.com"
        }

        val authDisplayName = authUser?.displayName.orEmpty().trim()
        val profileDisplayName = profileSnapshot?.childString("displayName").orEmpty().trim()
        val displayName = firstNotBlank(
            authDisplayName,
            profileDisplayName,
            email.substringBefore("@").trim(),
            if (isAnonymousUser) "" else userId,
        ).ifBlank { "Member" }

        val authPhoto = authUser?.photoUrl?.toString().orEmpty().trim()
        val profilePhoto = profileSnapshot?.childString("photoUrl").orEmpty().trim()
        val photoUrl = firstNotBlank(authPhoto, profilePhoto).takeIf { it.isNotBlank() }
        val identifier = firstNotBlank(
            profileSnapshot?.childString("identifier").orEmpty().trim(),
            profileSnapshot?.childString("deviceId").orEmpty().trim(),
        )

        if (authUser != null) {
            val profileMap = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "email" to email,
                "createdAtEpochMs" to System.currentTimeMillis(),
                "isAnonymous" to isAnonymousUser,
            )
            identifier.takeIf { it.isNotBlank() }?.let { profileMap["identifier"] = it }
            photoUrl?.let { profileMap["photoUrl"] = it }
            runCatching {
                root.child("users").child(userId).child("profile").updateChildren(profileMap).await()
            }
        }

        return Member(
            userId = userId,
            displayName = displayName,
            email = email,
            identifier = identifier,
            photoUrl = photoUrl,
            joinedAtEpochMs = joinedAt,
            role = role,
            active = true,
        )
    }

    private suspend fun enrichMemberFromProfileIfNeeded(member: Member): Member {
        val hasConcreteName = member.displayName.isNotBlank() &&
            !member.displayName.equals(member.userId, ignoreCase = true)
        val hasConcreteEmail = member.email.isNotBlank() &&
            !member.email.endsWith("@example.com", ignoreCase = true)
        val hasPhoto = !member.photoUrl.isNullOrBlank()
        if (hasConcreteName && hasConcreteEmail && hasPhoto) return member

        val profileSnapshot = runCatching {
            root.child("users").child(member.userId).child("profile").get().await()
        }.getOrNull() ?: return member

        val profileEmail = profileSnapshot.childString("email").ifBlank { member.email }
        val profileName = firstNotBlank(
            profileSnapshot.childString("displayName"),
            member.displayName,
            profileEmail.substringBefore("@"),
            member.userId,
        ).ifBlank { "Member" }
        val profilePhoto = profileSnapshot.childString("photoUrl").ifBlank { member.photoUrl.orEmpty() }
        val profileIdentifier = firstNotBlank(
            profileSnapshot.childString("identifier"),
            profileSnapshot.childString("deviceId"),
            member.identifier,
        )

        return member.copy(
            displayName = profileName.ifBlank { member.displayName },
            email = profileEmail.ifBlank { member.email },
            identifier = profileIdentifier,
            photoUrl = profilePhoto.ifBlank { member.photoUrl.orEmpty() }.ifBlank { null },
        )
    }

    private suspend fun resolveInviteTarget(inviteCode: String): InviteTarget {
        val normalizedInviteCode = normalizeInviteCode(inviteCode)
        require(normalizedInviteCode.isNotBlank()) { "Invite code is required." }

        val groupSnapshot = root.child("groups").get().await().children.firstOrNull { snapshot ->
            val flatCode = normalizeInviteCode(snapshot.childString("inviteCode"))
            val legacyCode = normalizeInviteCode(
                snapshot.child("invite").child("code").getValue(String::class.java).orEmpty(),
            )
            flatCode == normalizedInviteCode || legacyCode == normalizedInviteCode
        } ?: error("Invite code not found.")
        val group = groupSnapshot.toGroupOrNull() ?: error("Invalid group data.")
        check(group.active) { "Group is not active." }
        if (group.inviteExpiryEpochMs < System.currentTimeMillis()) {
            if (group.autoRenewInvite) {
                renewInviteInternal(group)
                error("Invite expired and a new code has been generated. Please ask an owner for the latest invite code.")
            }
            error("Invite has expired.")
        }
        return InviteTarget(
            snapshot = groupSnapshot,
            group = group,
        )
    }

    private suspend fun loadVisibleGroupForUser(groupId: String, userId: String): Group? {
        val group = root.child("groups").child(groupId).get().await().toGroupOrNull() ?: run {
            pruneStaleUserGroupReference(
                userId = userId,
                groupId = groupId,
                reason = "missing_group",
            )
            return null
        }
        if (!group.active) {
            pruneStaleUserGroupReference(
                userId = userId,
                groupId = groupId,
                reason = "inactive_group",
            )
            return null
        }

        val member = root.child("groupMembers").child(groupId).child(userId).get().await().toMemberOrNull()
        if (member == null || !member.active) {
            pruneStaleUserGroupReference(
                userId = userId,
                groupId = groupId,
                reason = "missing_or_inactive_member",
            )
            return null
        }

        return refreshInviteIfNeeded(group)
    }

    private suspend fun loadActiveMembers(groupId: String): List<Member> {
        return root.child("groupMembers").child(groupId).get().await().children
            .mapNotNull { it.toMemberOrNull() }
            .filter { it.active }
    }

    private suspend fun loadUserIdentityKeys(userId: String): Set<String> {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return emptySet()

        val profileSnapshot = runCatching {
            root.child("users").child(normalizedUserId).child("profile").get().await()
        }.getOrNull()
        val authUser = auth.currentUser?.takeIf { it.uid == normalizedUserId }
        return linkedSetOf(
            normalizedUserId,
            profileSnapshot?.childString("identifier").orEmpty().trim(),
            profileSnapshot?.childString("deviceId").orEmpty().trim(),
            authUser?.email.orEmpty().trim().lowercase(),
            profileSnapshot?.childString("email").orEmpty().trim().lowercase(),
        ).filter { it.isNotBlank() }.toSet()
    }

    private suspend fun refreshInviteIfNeeded(group: Group): Group {
        if (!group.autoRenewInvite || group.inviteExpiryEpochMs >= System.currentTimeMillis()) {
            return group
        }
        return renewInviteInternal(group)
    }

    private suspend fun pruneStaleUserGroupReference(
        userId: String,
        groupId: String,
        reason: String,
    ) {
        runCatching {
            root.child("userGroups").child(userId).child(groupId).removeValue().await()
        }.onFailure { throwable ->
            Log.w(
                "FirebaseGroupRepo",
                "Failed to prune stale userGroups link for userId=$userId, groupId=$groupId, reason=$reason.",
                throwable,
            )
        }
    }

    private suspend fun renewInviteInternal(group: Group): Group {
        val renewedGroup = group.copy(
            inviteCode = generateInviteCode(),
            inviteExpiryEpochMs = System.currentTimeMillis() + INVITE_EXPIRY_WEEK_MS,
        )
        root.child("groups").child(group.groupId).updateChildren(
            mapOf(
                "inviteCode" to renewedGroup.inviteCode,
                "inviteExpiryEpochMs" to renewedGroup.inviteExpiryEpochMs,
            ),
        ).await()
        return renewedGroup
    }

    private suspend fun ensureOwnerAccess(
        groupId: String,
        actorUserId: String,
        message: String = "Only an owner can perform this action.",
    ) {
        val actorRole = root.child("groupMembers")
            .child(groupId)
            .child(actorUserId)
            .child("role")
            .get()
            .await()
            .getValue(String::class.java)
            .orEmpty()
        val ownerUserId = root.child("groups")
            .child(groupId)
            .child("ownerUserId")
            .get()
            .await()
            .getValue(String::class.java)
            .orEmpty()
        check(actorRole == Role.OWNER.name || ownerUserId == actorUserId) { message }
    }

    private suspend fun mergeClaimedMemberIntoUser(
        group: Group,
        actualUserId: String,
        claimMemberUserId: String,
        existingMembers: List<Member>,
        existingActualMember: Member?,
    ): Group {
        val claimedMember = existingMembers.firstOrNull { member ->
            member.userId == claimMemberUserId && isManualMemberUserId(member.userId)
        } ?: error("Selected member is no longer available.")

        val mergedRole = if (
            existingActualMember?.role == Role.OWNER ||
            claimedMember.role == Role.OWNER ||
            group.ownerUserId == claimMemberUserId
        ) {
            Role.OWNER
        } else {
            Role.MEMBER
        }
        val mergedJoinedAt = minOf(
            existingActualMember?.joinedAtEpochMs ?: Long.MAX_VALUE,
            claimedMember.joinedAtEpochMs,
        ).takeUnless { it == Long.MAX_VALUE } ?: System.currentTimeMillis()
        val mergedMember = resolveMember(
            userId = actualUserId,
            joinedAt = mergedJoinedAt,
            role = mergedRole,
        )

        val updates = mutableMapOf<String, Any?>(
            "groupMembers/${group.groupId}/$claimMemberUserId" to null,
            "groupMembers/${group.groupId}/$actualUserId" to mergedMember.toFirebaseMap(),
            "userGroups/$actualUserId/${group.groupId}" to true,
            "userGroups/$claimMemberUserId/${group.groupId}" to null,
        )

        if (group.ownerUserId == claimMemberUserId) {
            updates["groups/${group.groupId}/ownerUserId"] = actualUserId
        }

        val expenses = root.child("expenses").child(group.groupId).get().await().children
            .mapNotNull { it.toExpenseOrNull() }
        expenses.forEach { expense ->
            val mergedExpense = expense.mergeMemberIntoUser(
                fromUserId = claimMemberUserId,
                toUserId = actualUserId,
            )
            if (mergedExpense != expense) {
                updates["expenses/${group.groupId}/${expense.expenseId}"] = mergedExpense.toFirebaseMap()
            }
        }

        val balanceMap = root.child("balances").child(group.groupId).get().await().children
            .associateNotNull(
                keySelector = { it.key },
                valueSelector = { it.childLongNullable("netPaise") },
            )
        if (balanceMap.isNotEmpty() || existingActualMember != null || claimedMember.userId.isNotBlank()) {
            val mergedBalances = balanceMap.mergeMemberBalanceIntoUser(
                fromUserId = claimMemberUserId,
                toUserId = actualUserId,
            )
            updates["balances/${group.groupId}"] = mergedBalances.mapValues { (userId, netPaise) ->
                mapOf(
                    "userId" to userId,
                    "netPaise" to netPaise,
                )
            }
        }

        val dispatchSnapshot = root.child("settlementDispatch").child(group.groupId).get().await()
        val dispatchMembers = dispatchSnapshot.child("members").children
            .mapNotNull { it.getValue(String::class.java) }
        if (dispatchMembers.isNotEmpty()) {
            val mergedDispatchMembers = dispatchMembers.mergeUserIdReferences(
                fromUserId = claimMemberUserId,
                toUserId = actualUserId,
            )
            if (mergedDispatchMembers != dispatchMembers) {
                updates["settlementDispatch/${group.groupId}/members"] = mergedDispatchMembers
                updates["settlementDispatch/${group.groupId}/memberCount"] = mergedDispatchMembers.size
            }
        }

        root.updateChildren(updates).await()
        return if (group.ownerUserId == claimMemberUserId) {
            group.copy(ownerUserId = actualUserId)
        } else {
            group
        }
    }

    private fun persistBalances(groupId: String, balances: Map<String, Long>) =
        root.child("balances").child(groupId).setValue(
            balances.mapValues { (userId, netPaise) ->
                mapOf(
                    "userId" to userId,
                    "netPaise" to netPaise,
                )
            },
        )

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString(8) {
            repeat(8) {
                append(chars[Random.nextInt(chars.length)])
            }
        }
    }

    private fun distributeAmountEvenly(totalAmount: Long, count: Int): List<Long> {
        if (count <= 0) return emptyList()
        val base = totalAmount / count
        var remainder = totalAmount % count
        return List(count) {
            var value = base
            if (remainder > 0) {
                value += 1
                remainder -= 1
            } else if (remainder < 0) {
                value -= 1
                remainder += 1
            }
            value
        }
    }
}

private data class InviteTarget(
    val snapshot: DataSnapshot,
    val group: Group,
)

private fun isManualMemberUserId(userId: String): Boolean {
    return userId.startsWith("guest_")
}

private const val INVITE_EXPIRY_WEEK_MS = 7L * 24L * 60L * 60L * 1000L

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

private fun firstNotBlank(vararg values: String): String {
    return values.firstOrNull { it.isNotBlank() }.orEmpty()
}

private fun normalizeInviteCode(raw: String): String {
    return raw
        .trim()
        .uppercase()
        .filter { it.isLetterOrDigit() }
}

private fun Member.matchesIdentityKeys(identityKeys: Set<String>): Boolean {
    if (identityKeys.isEmpty()) return false

    val normalizedEmail = email.trim().lowercase()
    return userId.trim() in identityKeys ||
        identifier.trim() in identityKeys ||
        (normalizedEmail.isNotBlank() && normalizedEmail in identityKeys)
}
