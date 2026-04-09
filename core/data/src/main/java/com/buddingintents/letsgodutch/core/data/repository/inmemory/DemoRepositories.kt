package com.buddingintents.letsgodutch.core.data.repository.inmemory

import com.buddingintents.letsgodutch.core.data.repository.AuthRepository
import com.buddingintents.letsgodutch.core.data.repository.ExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.GroupRepository
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberIntoUser
import com.buddingintents.letsgodutch.core.data.repository.PersonalExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.SettlementRepository
import com.buddingintents.letsgodutch.core.data.repository.TodoRepository
import com.buddingintents.letsgodutch.core.data.split.SplitCalculator
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.ExitLiabilityChoice
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.JoinGroupPreview
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import com.buddingintents.letsgodutch.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private object DemoStore {
    private const val ownerId = "user_owner"

    val user = MutableStateFlow<UserProfile?>(
        UserProfile(
            userId = ownerId,
            displayName = "Aditi",
            email = "aditi@example.com",
            createdAtEpochMs = System.currentTimeMillis(),
            identifier = "demo-device",
            deviceId = "demo-device",
            deviceModel = "InMemory Device",
            country = "India",
            primaryAuthProvider = "google.com",
            linkedProviders = listOf("google.com"),
        ),
    )
    val recentAnonymousDisplayNames = MutableStateFlow<List<String>>(emptyList())
    val anonymousProfilesByDeviceId = mutableMapOf<String, UserProfile>()

    val groups = MutableStateFlow<List<Group>>(emptyList())

    val membersByGroup = mutableMapOf<String, MutableStateFlow<List<Member>>>()

    val expensesByGroup = mutableMapOf<String, MutableStateFlow<List<Expense>>>()

    val todoByUser = mutableMapOf(
        ownerId to MutableStateFlow(
            listOf(
                TodoTask(
                    taskId = "todo_1",
                    userId = ownerId,
                    title = "Finalize weekend groceries",
                    status = TodoTaskStatus.ACTIVE,
                    createdAtEpochMs = System.currentTimeMillis() - 80_000L,
                    updatedAtEpochMs = System.currentTimeMillis() - 80_000L,
                ),
                TodoTask(
                    taskId = "todo_2",
                    userId = ownerId,
                    title = "Send payment reminder",
                    status = TodoTaskStatus.COMPLETED,
                    createdAtEpochMs = System.currentTimeMillis() - 150_000L,
                    updatedAtEpochMs = System.currentTimeMillis() - 20_000L,
                ),
            ),
        ),
    )

    val personalExpensesByUser = mutableMapOf(
        ownerId to MutableStateFlow(
            listOf(
                PersonalExpenseEntry(
                    expenseId = "pex_1",
                    userId = ownerId,
                    title = "Coffee",
                    amountPaise = 220_00,
                    spentAtEpochMs = System.currentTimeMillis() - 60_000L,
                    createdAtEpochMs = System.currentTimeMillis() - 60_000L,
                    updatedAtEpochMs = System.currentTimeMillis() - 60_000L,
                ),
            ),
        ),
    )
}

class InMemoryAuthRepository : AuthRepository {
    override val currentUser: Flow<UserProfile?> = DemoStore.user.asStateFlow()
    override fun observeRecentAnonymousDisplayNames(): Flow<List<String>> {
        return DemoStore.recentAnonymousDisplayNames.asStateFlow()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<UserProfile> {
        val now = System.currentTimeMillis()
        val current = DemoStore.user.value
        val value = if (current != null) {
            current.copy(
                email = current.email.ifBlank { "demo.user@example.com" },
                isAnonymous = false,
                primaryAuthProvider = "google.com",
                linkedProviders = listOf("google.com"),
                upgradedFromAnonymousAtEpochMs = current.upgradedFromAnonymousAtEpochMs
                    ?: now.takeIf { current.isAnonymous },
            )
        } else {
            UserProfile(
                userId = "user_$now",
                displayName = "New User",
                email = "newuser@example.com",
                createdAtEpochMs = now,
                identifier = "demo-device",
                deviceId = "demo-device",
                deviceModel = "InMemory Device",
                country = "India",
                primaryAuthProvider = "google.com",
                linkedProviders = listOf("google.com"),
            )
        }.also { DemoStore.user.value = it }
        return Result.success(value)
    }

    override suspend fun signInAnonymously(displayName: String): Result<UserProfile> {
        return runCatching {
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Display name is required." }
            val deviceId = "demo-device"
            val existingProfile = DemoStore.anonymousProfilesByDeviceId[deviceId]
            val profile = if (existingProfile != null) {
                existingProfile.copy(
                    displayName = normalizedName,
                    isAnonymous = true,
                )
            } else {
                UserProfile(
                    userId = "anon_${System.currentTimeMillis()}",
                    displayName = normalizedName,
                    email = "",
                    createdAtEpochMs = System.currentTimeMillis(),
                    isAnonymous = true,
                    identifier = deviceId,
                    deviceId = deviceId,
                    deviceModel = "InMemory Device",
                    country = "India",
                    primaryAuthProvider = "anonymous",
                    linkedProviders = listOf("anonymous"),
                )
            }
            DemoStore.anonymousProfilesByDeviceId[deviceId] = profile
            DemoStore.recentAnonymousDisplayNames.value = buildList {
                add(normalizedName)
                DemoStore.recentAnonymousDisplayNames.value.forEach { existing ->
                    if (!existing.equals(normalizedName, ignoreCase = true)) {
                        add(existing)
                    }
                }
            }.take(5)
            DemoStore.user.value = profile
            profile
        }
    }

    override suspend fun updateDisplayName(displayName: String): Result<UserProfile> {
        return runCatching {
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Display name is required." }

            val current = DemoStore.user.value ?: error("Please sign in again.")
            val updatedProfile = current.copy(displayName = normalizedName)
            DemoStore.user.value = updatedProfile

            DemoStore.membersByGroup.keys.forEach { groupId ->
                val membersFlow = DemoStore.membersByGroup[groupId] ?: return@forEach
                membersFlow.value = membersFlow.value.map { member ->
                    if (member.userId == updatedProfile.userId) {
                        member.copy(displayName = normalizedName)
                    } else {
                        member
                    }
                }
            }

            if (updatedProfile.isAnonymous) {
                DemoStore.anonymousProfilesByDeviceId[updatedProfile.deviceId.ifBlank { "demo-device" }] = updatedProfile
                DemoStore.recentAnonymousDisplayNames.value = buildList {
                    add(normalizedName)
                    DemoStore.recentAnonymousDisplayNames.value.forEach { existing ->
                        if (!existing.equals(normalizedName, ignoreCase = true)) {
                            add(existing)
                        }
                    }
                }.take(5)
            }

            updatedProfile
        }
    }

    override suspend fun signOut() {
        DemoStore.user.value = null
    }
}

class InMemoryGroupRepository : GroupRepository {
    override fun observeGroupsForUser(userId: String): Flow<List<Group>> {
        return DemoStore.groups.asStateFlow().map { groups ->
            val now = System.currentTimeMillis()
            val refreshedGroups = groups.map { group ->
                if (group.autoRenewInvite && group.inviteExpiryEpochMs < now) {
                    group.renewInvite(now = now)
                } else {
                    group
                }
            }
            if (refreshedGroups != groups) {
                DemoStore.groups.value = refreshedGroups
            }
            refreshedGroups.filter { group ->
                DemoStore.membersByGroup[group.groupId]
                    ?.value
                    ?.any { it.userId == userId && it.active } == true
            }
        }
    }

    override fun observeMembers(groupId: String): Flow<List<Member>> =
        DemoStore.membersByGroup[groupId]?.asStateFlow() ?: MutableStateFlow(emptyList())

    override suspend fun createGroup(
        name: String,
        ownerUserId: String,
        description: String,
        autoRenewInvite: Boolean,
        selectAllMembersByDefaultForExpenses: Boolean,
    ): Result<Group> {
        val now = System.currentTimeMillis()
        val group = Group(
            groupId = "group_${now}",
            name = name,
            ownerUserId = ownerUserId,
            createdAtEpochMs = now,
            description = description.trim(),
            inviteCode = "INV${now.toString().takeLast(4)}",
            inviteExpiryEpochMs = now + INVITE_EXPIRY_WEEK_MS,
            autoRenewInvite = autoRenewInvite,
            selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
        )
        DemoStore.groups.value = DemoStore.groups.value + group
        val ownerProfile = DemoStore.user.value
        DemoStore.membersByGroup[group.groupId] = MutableStateFlow(
            listOf(
                Member(
                    userId = ownerUserId,
                    displayName = ownerProfile?.displayName ?: "Owner",
                    email = ownerProfile?.email ?: "$ownerUserId@example.com",
                    identifier = ownerProfile?.identifier.orEmpty(),
                    joinedAtEpochMs = now,
                    role = Role.OWNER,
                ),
            ),
        )
        DemoStore.expensesByGroup[group.groupId] = MutableStateFlow(emptyList())
        return Result.success(group)
    }

    override suspend fun previewJoinWithInvite(inviteCode: String, userId: String): Result<JoinGroupPreview> {
        val normalizedInviteCode = inviteCode
            .trim()
            .uppercase()
            .filter { it.isLetterOrDigit() }
        val group = DemoStore.groups.value.firstOrNull { group ->
            group.inviteCode
                .trim()
                .uppercase()
                .filter { it.isLetterOrDigit() } == normalizedInviteCode
        }
            ?: return Result.failure(IllegalArgumentException("Invalid invite code"))
        if (group.inviteExpiryEpochMs < System.currentTimeMillis()) {
            if (group.autoRenewInvite) {
                val renewedGroup = group.renewInvite()
                DemoStore.groups.value = DemoStore.groups.value.map { existing ->
                    if (existing.groupId == renewedGroup.groupId) renewedGroup else existing
                }
                return Result.failure(
                    IllegalStateException(
                        "Invite expired and a new code has been generated. Please ask an owner for the latest invite code.",
                    ),
                )
            }
            return Result.failure(IllegalStateException("Invite has expired"))
        }
        val membersFlow = DemoStore.membersByGroup[group.groupId]
            ?: return Result.failure(IllegalStateException("Group member data missing"))
        val activeMembers = membersFlow.value.filter { it.active }
        val identityKeys = demoIdentityKeys(userId)
        val alreadyJoined = activeMembers.any { it.matchesIdentityKeys(identityKeys) }
        val claimableMembers = if (alreadyJoined) {
            emptyList()
        } else {
            activeMembers
                .filter { it.userId.startsWith("guest_") }
                .sortedWith(
                    compareByDescending<Member> { it.role == Role.OWNER }
                        .thenBy { it.joinedAtEpochMs },
                )
        }
        return Result.success(
            JoinGroupPreview(
                group = group,
                alreadyJoined = alreadyJoined,
                claimableMembers = claimableMembers,
            ),
        )
    }

    override suspend fun joinGroupWithInvite(
        inviteCode: String,
        userId: String,
        claimMemberUserId: String?,
    ): Result<Group> {
        val preview = previewJoinWithInvite(inviteCode = inviteCode, userId = userId)
            .getOrElse { return Result.failure(it) }
        val group = preview.group
        val membersFlow = DemoStore.membersByGroup[group.groupId]
            ?: return Result.failure(IllegalStateException("Group member data missing"))
        val activeMembers = membersFlow.value.filter { it.active }
        val existingActualMember = activeMembers.firstOrNull { it.userId == userId }

        if (claimMemberUserId.isNullOrBlank()) {
            if (activeMembers.size >= group.maxMembers && existingActualMember == null) {
                return Result.failure(IllegalStateException("Group is full"))
            }
            if (existingActualMember == null) {
                val profile = DemoStore.user.value?.takeIf { it.userId == userId }
                val displayName = profile?.displayName
                    ?.trim()
                    .orEmpty()
                    .ifBlank { "Member ${membersFlow.value.size + 1}" }
                val email = profile?.email?.trim().orEmpty()
                membersFlow.value = membersFlow.value + Member(
                    userId = userId,
                    displayName = displayName,
                    email = email,
                    identifier = profile?.identifier.orEmpty(),
                    photoUrl = profile?.photoUrl,
                    joinedAtEpochMs = System.currentTimeMillis(),
                )
            }
            return Result.success(group)
        }

        val claimedMember = activeMembers.firstOrNull { it.userId == claimMemberUserId && it.userId.startsWith("guest_") }
            ?: return Result.failure(IllegalArgumentException("Selected member is no longer available"))
        val profile = DemoStore.user.value?.takeIf { it.userId == userId }
        val mergedMember = Member(
            userId = userId,
            displayName = profile?.displayName
                ?.trim()
                .orEmpty()
                .ifBlank { claimedMember.displayName },
            email = profile?.email?.trim().orEmpty(),
            identifier = profile?.identifier
                ?.trim()
                .orEmpty()
                .ifBlank { existingActualMember?.identifier.orEmpty() },
            photoUrl = profile?.photoUrl ?: existingActualMember?.photoUrl,
            joinedAtEpochMs = minOf(
                existingActualMember?.joinedAtEpochMs ?: Long.MAX_VALUE,
                claimedMember.joinedAtEpochMs,
            ).takeUnless { it == Long.MAX_VALUE } ?: System.currentTimeMillis(),
            role = if (
                existingActualMember?.role == Role.OWNER ||
                claimedMember.role == Role.OWNER ||
                group.ownerUserId == claimedMember.userId
            ) {
                Role.OWNER
            } else {
                Role.MEMBER
            },
            active = true,
        )

        membersFlow.value = membersFlow.value
            .filterNot { it.userId == claimMemberUserId }
            .map { member ->
                if (member.userId == userId) {
                    mergedMember
                } else {
                    member
                }
            }
            .let { updatedMembers ->
                if (updatedMembers.none { it.userId == userId }) {
                    updatedMembers + mergedMember
                } else {
                    updatedMembers
                }
            }
            .sortedBy { it.joinedAtEpochMs }

        DemoStore.expensesByGroup[group.groupId]?.let { expensesFlow ->
            expensesFlow.value = expensesFlow.value.map { expense ->
                expense.mergeMemberIntoUser(
                    fromUserId = claimMemberUserId,
                    toUserId = userId,
                )
            }
        }

        if (group.ownerUserId == claimMemberUserId) {
            DemoStore.groups.value = DemoStore.groups.value.map { existingGroup ->
                if (existingGroup.groupId == group.groupId) {
                    existingGroup.copy(ownerUserId = userId)
                } else {
                    existingGroup
                }
            }
        }

        return Result.success(
            DemoStore.groups.value.firstOrNull { it.groupId == group.groupId } ?: group,
        )
    }

    override suspend fun updateGroupDetails(
        groupId: String,
        description: String,
        autoRenewInvite: Boolean,
        selectAllMembersByDefaultForExpenses: Boolean,
        actorUserId: String,
    ): Result<Group> {
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can update group details"))
        }

        val updated = group.copy(
            description = description.trim(),
            autoRenewInvite = autoRenewInvite,
            selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
        )
        DemoStore.groups.value = DemoStore.groups.value.map { existing ->
            if (existing.groupId == groupId) updated else existing
        }
        return Result.success(updated)
    }

    override suspend fun renewInvite(groupId: String, actorUserId: String): Result<Group> {
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can renew the invite"))
        }

        val updated = group.renewInvite()
        DemoStore.groups.value = DemoStore.groups.value.map { existing ->
            if (existing.groupId == groupId) updated else existing
        }
        return Result.success(updated)
    }

    override suspend fun addManualMember(groupId: String, displayName: String, actorUserId: String): Result<Member> {
        val normalizedName = displayName.trim()
        if (normalizedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Member name is required"))
        }
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        val membersFlow = DemoStore.membersByGroup[groupId]
            ?: return Result.failure(IllegalStateException("Group member data missing"))
        val activeMembers = membersFlow.value.filter { it.active }
        val actorIsActiveMember = activeMembers.any { it.userId == actorUserId }
        if (!actorIsActiveMember) {
            return Result.failure(IllegalAccessException("Only active group members can add members"))
        }
        if (activeMembers.size >= group.maxMembers) {
            return Result.failure(IllegalStateException("Group is full"))
        }
        val duplicate = activeMembers.any { it.displayName.trim().equals(normalizedName, ignoreCase = true) }
        if (duplicate) {
            return Result.failure(IllegalArgumentException("A member with this name already exists"))
        }

        val now = System.currentTimeMillis()
        val member = Member(
            userId = "guest_${groupId}_${now}",
            displayName = normalizedName,
            email = "",
            joinedAtEpochMs = now,
            role = Role.MEMBER,
            active = true,
        )
        membersFlow.value = membersFlow.value + member
        return Result.success(member)
    }

    override suspend fun updateMemberDisplayName(
        groupId: String,
        memberUserId: String,
        displayName: String,
        actorUserId: String,
    ): Result<Member> {
        val normalizedName = displayName.trim()
        if (normalizedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Member name is required"))
        }
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can edit member names"))
        }
        if (memberUserId == group.ownerUserId) {
            return Result.failure(IllegalArgumentException("Main owner name cannot be edited here"))
        }
        if (!memberUserId.startsWith("guest_")) {
            return Result.failure(IllegalArgumentException("Only manually added members can be edited"))
        }
        val membersFlow = DemoStore.membersByGroup[groupId]
            ?: return Result.failure(IllegalStateException("Group member data missing"))
        val activeMembers = membersFlow.value.filter { it.active }
        val target = activeMembers.firstOrNull { it.userId == memberUserId }
            ?: return Result.failure(IllegalArgumentException("Member not found"))
        val duplicate = activeMembers.any {
            it.userId != memberUserId && it.displayName.trim().equals(normalizedName, ignoreCase = true)
        }
        if (duplicate) {
            return Result.failure(IllegalArgumentException("A member with this name already exists"))
        }
        val updated = target.copy(displayName = normalizedName)
        membersFlow.value = membersFlow.value.map { member ->
            if (member.userId == memberUserId) updated else member
        }
        return Result.success(updated)
    }

    override suspend fun updateMemberRole(
        groupId: String,
        memberUserId: String,
        role: Role,
        actorUserId: String,
    ): Result<Member> {
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can manage owner roles"))
        }
        if (memberUserId == group.ownerUserId) {
            return Result.failure(IllegalArgumentException("Main owner role cannot be changed"))
        }
        val membersFlow = DemoStore.membersByGroup[groupId]
            ?: return Result.failure(IllegalStateException("Group member data missing"))
        val activeMembers = membersFlow.value.filter { it.active }
        val target = activeMembers.firstOrNull { it.userId == memberUserId }
            ?: return Result.failure(IllegalArgumentException("Member not found"))
        if (target.role == role) {
            return Result.success(target)
        }
        val updated = target.copy(role = role)
        membersFlow.value = membersFlow.value.map { member ->
            if (member.userId == memberUserId) updated else member
        }
        return Result.success(updated)
    }

    override suspend fun leaveGroup(
        groupId: String,
        userId: String,
        liabilityChoice: ExitLiabilityChoice,
    ): Result<Unit> {
        val membersFlow = DemoStore.membersByGroup[groupId]
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        membersFlow.value = membersFlow.value.filterNot { it.userId == userId }

        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (userId == group.ownerUserId) {
            val remainingMembers = membersFlow.value
            val nextMainOwner = remainingMembers
                .filter { it.role == Role.OWNER }
                .minByOrNull { it.joinedAtEpochMs }
                ?: remainingMembers.minByOrNull { it.joinedAtEpochMs }
            if (nextMainOwner != null) {
                DemoStore.groups.value = DemoStore.groups.value.map {
                    if (it.groupId == groupId) it.copy(ownerUserId = nextMainOwner.userId) else it
                }
                membersFlow.value = membersFlow.value.map { member ->
                    if (member.userId == nextMainOwner.userId && member.role != Role.OWNER) {
                        member.copy(role = Role.OWNER)
                    } else {
                        member
                    }
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun removeMember(
        groupId: String,
        memberUserId: String,
        actorUserId: String,
        liabilityChoice: ExitLiabilityChoice,
    ): Result<Unit> {
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can remove members"))
        }
        if (memberUserId == group.ownerUserId) {
            return Result.failure(IllegalArgumentException("Main owner cannot be removed"))
        }
        return leaveGroup(
            groupId = groupId,
            userId = memberUserId,
            liabilityChoice = liabilityChoice,
        )
    }

    override suspend fun deleteGroup(groupId: String, actorUserId: String): Result<Unit> {
        val group = DemoStore.groups.value.firstOrNull { it.groupId == groupId }
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (!canManageGroup(groupId = groupId, actorUserId = actorUserId)) {
            return Result.failure(IllegalAccessException("Only an owner can delete the group"))
        }

        DemoStore.groups.value = DemoStore.groups.value.filterNot { it.groupId == groupId }
        DemoStore.membersByGroup.remove(groupId)
        DemoStore.expensesByGroup.remove(groupId)
        return Result.success(Unit)
    }
}

class InMemoryExpenseRepository : ExpenseRepository {
    override fun observeExpenses(groupId: String): Flow<List<Expense>> =
        DemoStore.expensesByGroup[groupId]?.asStateFlow() ?: MutableStateFlow(emptyList())

    override fun observeBalances(groupId: String): Flow<List<Balance>> {
        return observeExpenses(groupId).map { expenses ->
            val balances = mutableMapOf<String, Long>()
            expenses.forEach { expense ->
                val split = SplitCalculator.allocate(
                    totalPaise = expense.amountPaise,
                    participantUserIds = expense.participantUserIds,
                    splitType = expense.splitType,
                    shares = expense.shares,
                ).getOrElse {
                    SplitCalculator.allocate(
                        totalPaise = expense.amountPaise,
                        participantUserIds = expense.participantUserIds,
                        splitType = SplitType.EQUAL,
                        shares = emptyList(),
                    ).getOrThrow()
                }
                split.forEach { (userId, amount) ->
                    balances[userId] = (balances[userId] ?: 0L) - amount
                }
                balances[expense.paidByUserId] = (balances[expense.paidByUserId] ?: 0L) + expense.amountPaise
            }
            balances.map { (userId, value) -> Balance(userId = userId, netPaise = value) }
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        val flow = DemoStore.expensesByGroup[expense.groupId]
            ?: return Result.failure(IllegalArgumentException("Group not found"))

        val finalizedExpense = when (expense.splitType) {
            SplitType.EQUAL -> expense.copy(shares = emptyList())
            SplitType.EXACT,
            SplitType.PERCENTAGE,
            SplitType.CUSTOM,
            -> expense
        }

        flow.value = flow.value + finalizedExpense
        return Result.success(Unit)
    }

    override suspend fun updateExpense(expense: Expense, actorUserId: String): Result<Unit> {
        val flow = DemoStore.expensesByGroup[expense.groupId]
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        val current = flow.value.firstOrNull { it.expenseId == expense.expenseId }
            ?: return Result.failure(IllegalArgumentException("Expense not found"))
        val isOwner = DemoStore.groups.value
            .firstOrNull { it.groupId == expense.groupId }
            ?.let { group ->
                DemoStore.membersByGroup[group.groupId]
                    ?.value
                    ?.firstOrNull { it.userId == actorUserId && it.active }
                    ?.role == Role.OWNER
            } == true
        if (current.createdByUserId != actorUserId && !isOwner) {
            return Result.failure(IllegalAccessException("Only an owner or creator can edit"))
        }
        flow.value = flow.value.map {
            if (it.expenseId == expense.expenseId) expense.copy(updatedAtEpochMs = System.currentTimeMillis()) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteExpense(groupId: String, expenseId: String, actorUserId: String): Result<Unit> {
        val flow = DemoStore.expensesByGroup[groupId]
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        val current = flow.value.firstOrNull { it.expenseId == expenseId }
            ?: return Result.failure(IllegalArgumentException("Expense not found"))
        val isOwner = DemoStore.groups.value
            .firstOrNull { it.groupId == groupId }
            ?.let { group ->
                DemoStore.membersByGroup[group.groupId]
                    ?.value
                    ?.firstOrNull { it.userId == actorUserId && it.active }
                    ?.role == Role.OWNER
            } == true
        if (current.createdByUserId != actorUserId && !isOwner) {
            return Result.failure(IllegalAccessException("Only an owner or creator can delete"))
        }
        flow.value = flow.value.filterNot { it.expenseId == expenseId }
        return Result.success(Unit)
    }
}

class InMemoryTodoRepository : TodoRepository {
    override fun observeTasks(userId: String): Flow<List<TodoTask>> {
        return taskFlowFor(userId).asStateFlow()
    }

    override suspend fun addTask(userId: String, title: String): Result<TodoTask> {
        val normalizedTitle = title.trim()
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id is required"))
        }
        if (normalizedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title is required"))
        }
        val now = System.currentTimeMillis()
        val task = TodoTask(
            taskId = "todo_${userId}_$now",
            userId = userId,
            title = normalizedTitle,
            status = TodoTaskStatus.ACTIVE,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        val flow = taskFlowFor(userId)
        flow.value = listOf(task) + flow.value
        return Result.success(task)
    }

    override suspend fun updateTaskStatus(
        userId: String,
        taskId: String,
        status: TodoTaskStatus,
    ): Result<TodoTask> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id is required"))
        }
        if (taskId.isBlank()) {
            return Result.failure(IllegalArgumentException("Task id is required"))
        }
        val flow = taskFlowFor(userId)
        val current = flow.value.firstOrNull { it.taskId == taskId }
            ?: return Result.failure(IllegalArgumentException("Task not found"))
        val updated = current.copy(
            status = status,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        flow.value = flow.value.map { if (it.taskId == taskId) updated else it }
        return Result.success(updated)
    }

    override suspend fun deleteTask(
        userId: String,
        taskId: String,
    ): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id is required"))
        }
        if (taskId.isBlank()) {
            return Result.failure(IllegalArgumentException("Task id is required"))
        }
        val flow = taskFlowFor(userId)
        if (flow.value.none { it.taskId == taskId }) {
            return Result.failure(IllegalArgumentException("Task not found"))
        }
        flow.value = flow.value.filterNot { it.taskId == taskId }
        return Result.success(Unit)
    }

    private fun taskFlowFor(userId: String): MutableStateFlow<List<TodoTask>> {
        return DemoStore.todoByUser.getOrPut(userId) { MutableStateFlow(emptyList()) }
    }
}

class InMemoryPersonalExpenseRepository : PersonalExpenseRepository {
    override fun observeExpenses(userId: String): Flow<List<PersonalExpenseEntry>> {
        return expenseFlowFor(userId).asStateFlow()
    }

    override suspend fun addExpense(
        userId: String,
        title: String,
        amountPaise: Long,
        spentAtEpochMs: Long,
    ): Result<PersonalExpenseEntry> {
        val normalizedTitle = title.trim()
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id is required"))
        }
        if (normalizedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Expense title is required"))
        }
        if (amountPaise <= 0L) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val now = System.currentTimeMillis()
        val expense = PersonalExpenseEntry(
            expenseId = "pex_${userId}_$now",
            userId = userId,
            title = normalizedTitle,
            amountPaise = amountPaise,
            spentAtEpochMs = spentAtEpochMs,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        val flow = expenseFlowFor(userId)
        flow.value = listOf(expense) + flow.value
        return Result.success(expense)
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id is required"))
        }
        if (expenseId.isBlank()) {
            return Result.failure(IllegalArgumentException("Expense id is required"))
        }
        val flow = expenseFlowFor(userId)
        flow.value = flow.value.filterNot { it.expenseId == expenseId }
        return Result.success(Unit)
    }

    private fun expenseFlowFor(userId: String): MutableStateFlow<List<PersonalExpenseEntry>> {
        return DemoStore.personalExpensesByUser.getOrPut(userId) { MutableStateFlow(emptyList()) }
    }
}

class InMemorySettlementRepository : SettlementRepository {
    override suspend fun generateSettlementPdf(groupId: String, actorUserId: String): Result<String> {
        val hasGroup = DemoStore.groups.value.any { it.groupId == groupId }
        if (!hasGroup) {
            return Result.failure(IllegalArgumentException("Group not found"))
        }
        return Result.success("file://settlement_$groupId.pdf")
    }

    override suspend fun dispatchSettlementPdfToMembers(
        groupId: String,
        actorUserId: String,
        pdfPath: String,
    ): Result<Unit> {
        val hasGroup = DemoStore.groups.value.any { it.groupId == groupId }
        if (!hasGroup) {
            return Result.failure(IllegalArgumentException("Group not found"))
        }
        if (pdfPath.isBlank()) {
            return Result.failure(IllegalArgumentException("PDF path missing"))
        }
        return Result.success(Unit)
    }

    override suspend fun markGroupSettled(groupId: String, actorUserId: String): Result<Unit> {
        if (DemoStore.groups.value.none { it.groupId == groupId }) {
            return Result.failure(IllegalArgumentException("Group not found"))
        }
        val isOwner = DemoStore.membersByGroup[groupId]
            ?.value
            ?.firstOrNull { it.userId == actorUserId && it.active }
            ?.role == Role.OWNER
        if (isOwner != true) {
            return Result.failure(IllegalAccessException("Only an owner can settle"))
        }
        val flow = DemoStore.expensesByGroup[groupId]
            ?: return Result.failure(IllegalArgumentException("Group not found"))
        if (flow.value.isEmpty()) {
            return Result.failure(IllegalStateException("No expenses to settle."))
        }
        flow.value = emptyList()
        return Result.success(Unit)
    }
}

private fun canManageGroup(groupId: String, actorUserId: String): Boolean {
    return DemoStore.membersByGroup[groupId]
        ?.value
        ?.firstOrNull { it.userId == actorUserId && it.active }
        ?.role == Role.OWNER
}

private fun Group.renewInvite(now: Long = System.currentTimeMillis()): Group {
    return copy(
        inviteCode = "INV${now.toString().takeLast(6)}",
        inviteExpiryEpochMs = now + INVITE_EXPIRY_WEEK_MS,
    )
}

private fun demoIdentityKeys(userId: String): Set<String> {
    val profile = DemoStore.user.value?.takeIf { it.userId == userId }
    return linkedSetOf(
        userId.trim(),
        profile?.identifier.orEmpty().trim(),
        profile?.deviceId.orEmpty().trim(),
        profile?.email.orEmpty().trim().lowercase(),
    ).filter { it.isNotBlank() }.toSet()
}

private fun Member.matchesIdentityKeys(identityKeys: Set<String>): Boolean {
    if (identityKeys.isEmpty()) return false

    val normalizedEmail = email.trim().lowercase()
    return userId.trim() in identityKeys ||
        identifier.trim() in identityKeys ||
        (normalizedEmail.isNotBlank() && normalizedEmail in identityKeys)
}

private const val INVITE_EXPIRY_WEEK_MS = 7L * 24L * 60L * 60L * 1000L
