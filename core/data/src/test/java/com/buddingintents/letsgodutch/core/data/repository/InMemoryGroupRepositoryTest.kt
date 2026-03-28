package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryGroupRepository
import com.buddingintents.letsgodutch.core.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryGroupRepositoryTest {

    @Test
    fun `createGroup stores description invite renewal and expense defaults`() = runBlocking {
        val repository = InMemoryGroupRepository()

        val group = repository.createGroup(
            name = "Test Group",
            ownerUserId = "owner_test",
            description = "Weekend planning",
            autoRenewInvite = false,
            selectAllMembersByDefaultForExpenses = true,
        ).getOrThrow()

        assertEquals("Weekend planning", group.description)
        assertFalse(group.autoRenewInvite)
        assertTrue(group.selectAllMembersByDefaultForExpenses)
    }

    @Test
    fun `previewJoinWithInvite keeps owner placeholder claimable`() = runBlocking {
        val repository = InMemoryGroupRepository()
        val ownerUserId = "owner_claim_test"
        val group = repository.createGroup(
            name = "Claim Test",
            ownerUserId = ownerUserId,
            description = "",
            autoRenewInvite = true,
            selectAllMembersByDefaultForExpenses = false,
        ).getOrThrow()

        val placeholder = repository.addManualMember(
            groupId = group.groupId,
            displayName = "Future Owner",
            actorUserId = ownerUserId,
        ).getOrThrow()
        repository.updateMemberRole(
            groupId = group.groupId,
            memberUserId = placeholder.userId,
            role = Role.OWNER,
            actorUserId = ownerUserId,
        ).getOrThrow()

        val preview = repository.previewJoinWithInvite(
            inviteCode = group.inviteCode,
            userId = "new_actual_user",
        ).getOrThrow()

        assertTrue(preview.claimableMembers.isNotEmpty())
        assertEquals(placeholder.userId, preview.claimableMembers.first().userId)
        assertEquals(Role.OWNER, preview.claimableMembers.first().role)
    }
}
