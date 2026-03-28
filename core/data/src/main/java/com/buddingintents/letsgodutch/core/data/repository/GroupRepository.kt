package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.ExitLiabilityChoice
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.JoinGroupPreview
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroupsForUser(userId: String): Flow<List<Group>>
    fun observeMembers(groupId: String): Flow<List<Member>>
    suspend fun createGroup(
        name: String,
        ownerUserId: String,
        description: String = "",
        autoRenewInvite: Boolean = true,
        selectAllMembersByDefaultForExpenses: Boolean = false,
    ): Result<Group>
    suspend fun previewJoinWithInvite(inviteCode: String, userId: String): Result<JoinGroupPreview>
    suspend fun joinGroupWithInvite(
        inviteCode: String,
        userId: String,
        claimMemberUserId: String? = null,
    ): Result<Group>
    suspend fun updateGroupDetails(
        groupId: String,
        description: String,
        autoRenewInvite: Boolean,
        selectAllMembersByDefaultForExpenses: Boolean,
        actorUserId: String,
    ): Result<Group>
    suspend fun renewInvite(groupId: String, actorUserId: String): Result<Group>
    suspend fun addManualMember(groupId: String, displayName: String, actorUserId: String): Result<Member>
    suspend fun updateMemberDisplayName(
        groupId: String,
        memberUserId: String,
        displayName: String,
        actorUserId: String,
    ): Result<Member>
    suspend fun updateMemberRole(
        groupId: String,
        memberUserId: String,
        role: Role,
        actorUserId: String,
    ): Result<Member>
    suspend fun leaveGroup(groupId: String, userId: String, liabilityChoice: ExitLiabilityChoice): Result<Unit>
    suspend fun removeMember(
        groupId: String,
        memberUserId: String,
        actorUserId: String,
        liabilityChoice: ExitLiabilityChoice,
    ): Result<Unit>
    suspend fun deleteGroup(groupId: String, actorUserId: String): Result<Unit>
}
