package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberBalanceIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeMemberIntoUser
import com.buddingintents.letsgodutch.core.data.repository.mergeUserIdReferences
import com.buddingintents.letsgodutch.core.data.repository.AuthRepository
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val context: Context,
) : AuthRepository {
    private val anonymousSessionStore = AnonymousSessionSecureStore(context)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val currentUserState = MutableStateFlow<UserProfile?>(null)
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        repositoryScope.launch {
            publishCurrentUser(firebaseAuth.currentUser)
        }
    }

    override val currentUser: Flow<UserProfile?> = currentUserState.asStateFlow()

    init {
        auth.addAuthStateListener(authStateListener)
        repositoryScope.launch {
            publishCurrentUser(auth.currentUser)
        }
    }

    override fun observeRecentAnonymousDisplayNames(): Flow<List<String>> {
        return anonymousSessionStore.recentNames
    }

    private suspend fun publishCurrentUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            currentUserState.value = null
            return
        }
        if (firebaseUser.isAnonymous && anonymousSessionStore.isAnonymousSessionHidden(firebaseUser.uid)) {
            currentUserState.value = null
            return
        }
        val profile = syncAndPersistProfile(
            firebaseUser = firebaseUser,
            deviceContext = context.currentDeviceContext(),
        )
        runCatching { syncGroupMemberProfile(profile) }
        currentUserState.value = profile
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<UserProfile> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: error("Google sign-in completed but user is null")
            anonymousSessionStore.clearHiddenAnonymousSession()
            val profile = syncAndPersistProfile(
                firebaseUser = firebaseUser,
                deviceContext = context.currentDeviceContext(),
                strictPersist = true,
            )
            runCatching { syncGroupMemberProfile(profile) }
            currentUserState.value = profile
            profile
        }
    }

    override suspend fun signInAnonymously(displayName: String): Result<UserProfile> {
        return try {
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Display name is required." }
            val deviceContext = context.currentDeviceContext()

            val firebaseUser = auth.currentUser
                ?.takeIf { it.isAnonymous }
                ?: run {
                    val authResult = auth.signInAnonymously().await()
                    authResult.user ?: error("Anonymous sign-in completed but user is null")
                }
            runCatching {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(normalizedName)
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()
            }
            val profile = syncAndPersistProfile(
                firebaseUser = firebaseUser,
                preferredDisplayName = normalizedName,
                preferredEmail = "",
                deviceContext = deviceContext,
                strictPersist = true,
            )
            migrateAnonymousAccountIfNeeded(
                currentProfile = profile,
                deviceContext = deviceContext,
            )
            persistAnonymousIdentifierBinding(
                identifier = profile.identifier,
                userId = profile.userId,
            )
            anonymousSessionStore.clearHiddenAnonymousSession(userId = firebaseUser.uid)
            anonymousSessionStore.recordAnonymousDisplayName(normalizedName)
            val refreshedProfile = syncAndPersistProfile(
                firebaseUser = firebaseUser,
                preferredDisplayName = normalizedName,
                preferredEmail = "",
                deviceContext = deviceContext,
                strictPersist = true,
            )
            runCatching { syncGroupMemberProfile(refreshedProfile) }
            currentUserState.value = refreshedProfile
            Result.success(refreshedProfile)
        } catch (error: Throwable) {
            Result.failure(mapAnonymousSignInError(error))
        }
    }

    override suspend fun updateDisplayName(displayName: String): Result<UserProfile> {
        return runCatching {
            val normalizedName = displayName.trim()
            require(normalizedName.isNotBlank()) { "Display name is required." }

            val firebaseUser = auth.currentUser ?: error("Please sign in again.")
            runCatching {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(normalizedName)
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()
            }

            val updatedProfile = syncAndPersistProfile(
                firebaseUser = firebaseUser,
                preferredDisplayName = normalizedName,
                deviceContext = context.currentDeviceContext(),
                strictPersist = true,
            )
            runCatching { syncGroupMemberProfile(updatedProfile) }
            if (updatedProfile.isAnonymous) {
                anonymousSessionStore.recordAnonymousDisplayName(updatedProfile.displayName)
            }
            currentUserState.value = updatedProfile
            updatedProfile
        }
    }

    override suspend fun signOut() {
        val currentUser = auth.currentUser
        if (currentUser?.isAnonymous == true) {
            anonymousSessionStore.markAnonymousSessionHidden(currentUser.uid)
            currentUserState.value = null
            return
        }
        auth.signOut()
        anonymousSessionStore.clearHiddenAnonymousSession()
        currentUserState.value = null
        runCatching {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private suspend fun syncGroupMemberProfile(
        profile: UserProfile,
    ) {
        val groupIds = loadKnownGroupIdsForUser(profile.userId)
        if (groupIds.isEmpty()) {
            if (profile.isAnonymous) {
                runCatching {
                    persistAnonymousIdentifierBinding(
                        identifier = profile.identifier.ifBlank { profile.deviceId },
                        userId = profile.userId,
                    )
                }
            }
            return
        }

        val updates = mutableMapOf<String, Any?>()
        val now = System.currentTimeMillis()
        groupIds.forEach { groupId ->
            val groupSnapshot = runCatching {
                database.reference.child("groups").child(groupId).get().await()
            }.getOrNull()
            val group = groupSnapshot?.toGroupOrNull() ?: run {
                updates["userGroups/${profile.userId}/$groupId"] = null
                return@forEach
            }
            val memberSnapshot = database.reference
                .child("groupMembers")
                .child(groupId)
                .child(profile.userId)
                .get()
                .await()
            val existingMember = memberSnapshot.takeIf { it.exists() }?.toMemberOrNull()
            val isOwner = group.ownerUserId == profile.userId

            if (existingMember == null && !isOwner) {
                updates["userGroups/${profile.userId}/$groupId"] = null
                return@forEach
            }
            if (existingMember?.active == false) {
                updates["userGroups/${profile.userId}/$groupId"] = null
                return@forEach
            }

            val syncedMember = Member(
                userId = profile.userId,
                displayName = firstNotBlank(
                    profile.displayName,
                    existingMember?.displayName.orEmpty(),
                    if (profile.isAnonymous) "" else profile.userId,
                    "Member",
                ),
                email = firstNotBlank(
                    profile.email,
                    existingMember?.email.orEmpty(),
                ).ifBlank {
                    if (profile.isAnonymous) "" else "${profile.userId}@example.com"
                },
                identifier = firstNotBlank(
                    profile.identifier,
                    profile.deviceId,
                    existingMember?.identifier.orEmpty(),
                ),
                photoUrl = profile.photoUrl ?: existingMember?.photoUrl,
                joinedAtEpochMs = existingMember?.joinedAtEpochMs
                    ?.takeIf { it > 0L }
                    ?: memberSnapshot.childLongNullable("joinedAt")
                    ?: group.createdAtEpochMs.takeIf { it > 0L }
                    ?: now,
                role = when {
                    isOwner -> Role.OWNER
                    existingMember != null -> existingMember.role
                    else -> Role.MEMBER
                },
                active = true,
            )

            updates["groupMembers/$groupId/${profile.userId}"] = syncedMember.toFirebaseMap()
            updates["userGroups/${profile.userId}/$groupId"] = true
        }
        if (updates.isNotEmpty()) {
            database.reference.updateChildren(updates).await()
        }
        if (profile.isAnonymous) {
            runCatching {
                persistAnonymousIdentifierBinding(
                    identifier = profile.identifier.ifBlank { profile.deviceId },
                    userId = profile.userId,
                )
            }
        }
    }

    private suspend fun syncAndPersistProfile(
        firebaseUser: FirebaseUser,
        preferredDisplayName: String = "",
        preferredEmail: String = "",
        deviceContext: DeviceContext = context.currentDeviceContext(),
        strictPersist: Boolean = false,
    ): UserProfile {
        val profileRef = database.reference
            .child("users")
            .child(firebaseUser.uid)
            .child("profile")
        val profileSnapshot = runCatching { profileRef.get().await() }.getOrNull()
        val profile = buildProfile(
            firebaseUser = firebaseUser,
            profileSnapshot = profileSnapshot,
            preferredDisplayName = preferredDisplayName,
            preferredEmail = preferredEmail,
            deviceContext = deviceContext,
        )
        if (strictPersist) {
            profileRef.updateChildren(profile.toFirebaseProfileMap()).await()
        } else {
            runCatching {
                profileRef.updateChildren(profile.toFirebaseProfileMap()).await()
            }
        }
        return profile
    }

    private fun buildProfile(
        firebaseUser: FirebaseUser,
        profileSnapshot: DataSnapshot?,
        preferredDisplayName: String = "",
        preferredEmail: String = "",
        deviceContext: DeviceContext,
    ): UserProfile {
        val snapshotDisplayName = profileSnapshot.childString("displayName").trim()
        val snapshotEmail = profileSnapshot.childString("email").trim()
        val snapshotPhotoUrl = profileSnapshot.childString("photoUrl").trim()
        val snapshotCreatedAt = profileSnapshot.childLongNullable("createdAtEpochMs")
            ?: profileSnapshot.childLongNullable("createdAt")
            ?: 0L
        val snapshotIdentifier = profileSnapshot.childString("identifier").trim()
        val snapshotDeviceId = profileSnapshot.childString("deviceId").trim()
        val snapshotDeviceModel = profileSnapshot.childString("deviceModel").trim()
        val snapshotCountry = profileSnapshot.childString("country").trim()
        val now = System.currentTimeMillis()

        val firebaseDisplayName = firebaseUser.displayName.orEmpty().trim()
        val firebasePhotoUrl = firebaseUser.photoUrl?.toString().orEmpty().trim()

        val resolvedDisplayName = firstNotBlank(
            preferredDisplayName.trim(),
            snapshotDisplayName,
            firebaseDisplayName,
            if (firebaseUser.isAnonymous) "" else firebaseUser.canonicalEmail().substringBefore("@"),
            "Member",
        )
        val resolvedEmail = firstNotBlank(
            preferredEmail.trim(),
            snapshotEmail,
            firebaseUser.canonicalEmail(),
        )
        val resolvedPhoto = firstNotBlank(
            firebasePhotoUrl,
            snapshotPhotoUrl,
        ).ifBlank { null }

        return UserProfile(
            userId = firebaseUser.uid,
            displayName = resolvedDisplayName,
            email = resolvedEmail,
            photoUrl = resolvedPhoto,
            createdAtEpochMs = if (snapshotCreatedAt > 0L) snapshotCreatedAt else now,
            isAnonymous = firebaseUser.isAnonymous,
            identifier = firstNotBlank(deviceContext.deviceId, snapshotIdentifier, snapshotDeviceId),
            deviceId = firstNotBlank(deviceContext.deviceId, snapshotDeviceId, snapshotIdentifier),
            deviceModel = firstNotBlank(deviceContext.deviceModel, snapshotDeviceModel),
            country = firstNotBlank(deviceContext.country, snapshotCountry),
        )
    }

    private suspend fun migrateAnonymousAccountIfNeeded(
        currentProfile: UserProfile,
        deviceContext: DeviceContext,
    ) {
        if (!currentProfile.isAnonymous) return
        val identifier = currentProfile.identifier.trim().ifBlank { deviceContext.deviceId.trim() }
        if (identifier.isBlank()) return

        val boundUserId = resolveBoundAnonymousUserId(
            identifier = identifier,
            currentUserId = currentProfile.userId,
        )

        if (boundUserId.isBlank() || boundUserId == currentProfile.userId) return

        val updates = mutableMapOf<String, Any?>()
        mergeGroupMemberships(
            fromUserId = boundUserId,
            toProfile = currentProfile,
            updates = updates,
        )
        mergeUserOwnedNode(
            rootPath = "todoTasks",
            fromUserId = boundUserId,
            toUserId = currentProfile.userId,
            updates = updates,
            userIdFieldName = "userId",
        )
        mergeUserOwnedNode(
            rootPath = "personalExpenses",
            fromUserId = boundUserId,
            toUserId = currentProfile.userId,
            updates = updates,
            userIdFieldName = "userId",
        )
        mergeUserOwnedNode(
            rootPath = "notifications",
            fromUserId = boundUserId,
            toUserId = currentProfile.userId,
            updates = updates,
        )
        mergeUserOwnedNode(
            rootPath = "fcmTokens",
            fromUserId = boundUserId,
            toUserId = currentProfile.userId,
            updates = updates,
        )
        updates["$ANONYMOUS_DEVICES_NODE/$identifier/userId"] = currentProfile.userId
        updates["$ANONYMOUS_DEVICES_NODE/$identifier/identifier"] = identifier
        updates["$ANONYMOUS_DEVICES_NODE/$identifier/updatedAtEpochMs"] = System.currentTimeMillis()

        if (updates.isNotEmpty()) {
            database.reference.updateChildren(updates).await()
        }
    }

    private suspend fun persistAnonymousIdentifierBinding(
        identifier: String,
        userId: String,
    ) {
        val normalizedIdentifier = identifier.trim()
        val normalizedUserId = userId.trim()
        if (normalizedIdentifier.isBlank() || normalizedUserId.isBlank()) return

        database.reference.child(ANONYMOUS_DEVICES_NODE).child(normalizedIdentifier)
            .updateChildren(
                mapOf(
                    "userId" to normalizedUserId,
                    "identifier" to normalizedIdentifier,
                    "updatedAtEpochMs" to System.currentTimeMillis(),
                ),
            )
            .await()
    }

    private suspend fun resolveBoundAnonymousUserId(
        identifier: String,
        currentUserId: String,
    ): String {
        val normalizedIdentifier = identifier.trim()
        if (normalizedIdentifier.isBlank()) return ""

        val directBinding = runCatching {
            database.reference
                .child(ANONYMOUS_DEVICES_NODE)
                .child(normalizedIdentifier)
                .child("userId")
                .get()
                .await()
                .getValue(String::class.java)
                .orEmpty()
                .trim()
        }.getOrDefault("")

        if (directBinding.isNotBlank() && directBinding != currentUserId) {
            return directBinding
        }

        return findBoundAnonymousUserIdByQuery(
            queryChildPath = "profile/identifier",
            identifier = normalizedIdentifier,
            currentUserId = currentUserId,
        ).ifBlank {
            findBoundAnonymousUserIdByQuery(
                queryChildPath = "profile/deviceId",
                identifier = normalizedIdentifier,
                currentUserId = currentUserId,
            )
        }
    }

    private suspend fun findBoundAnonymousUserIdByQuery(
        queryChildPath: String,
        identifier: String,
        currentUserId: String,
    ): String {
        val matchingUsers = runCatching {
            database.reference
                .child("users")
                .orderByChild(queryChildPath)
                .equalTo(identifier)
                .get()
                .await()
        }.getOrNull() ?: return ""

        val candidates = matchingUsers.children.mapNotNull { userSnapshot ->
            val candidateUserId = userSnapshot.key.orEmpty().trim()
            if (candidateUserId.isBlank() || candidateUserId == currentUserId) {
                return@mapNotNull null
            }

            val profileSnapshot = userSnapshot.child("profile")
            val candidateIdentifier = firstNotBlank(
                profileSnapshot.childString("identifier").trim(),
                profileSnapshot.childString("deviceId").trim(),
            )
            if (candidateIdentifier != identifier) return@mapNotNull null
            if (profileSnapshot.child("isAnonymous").getValue(Boolean::class.java) != true) {
                return@mapNotNull null
            }

            val hasGroupMembership = runCatching {
                loadKnownGroupIdsForUser(candidateUserId).isNotEmpty()
            }.getOrDefault(false)

            AnonymousAccountCandidate(
                userId = candidateUserId,
                hasGroupMembership = hasGroupMembership,
                createdAtEpochMs = profileSnapshot.childLongNullable("createdAtEpochMs") ?: 0L,
            )
        }

        return candidates
            .sortedWith(
                compareByDescending<AnonymousAccountCandidate> { it.hasGroupMembership }
                    .thenByDescending { it.createdAtEpochMs },
            )
            .firstOrNull()
            ?.userId
            .orEmpty()
    }

    private suspend fun mergeGroupMemberships(
        fromUserId: String,
        toProfile: UserProfile,
        updates: MutableMap<String, Any?>,
    ) {
        val groupIds = loadKnownGroupIdsForUser(fromUserId)

        groupIds.forEach { groupId ->
            val groupSnapshot = database.reference.child("groups").child(groupId).get().await()
            val group = groupSnapshot.toGroupOrNull() ?: run {
                updates["userGroups/$fromUserId/$groupId"] = null
                return@forEach
            }

            val members = database.reference.child("groupMembers").child(groupId).get().await().children
                .mapNotNull { it.toMemberOrNull() }
                .filter { it.active }
            val sourceMember = members.firstOrNull { it.userId == fromUserId } ?: run {
                updates["userGroups/$fromUserId/$groupId"] = null
                return@forEach
            }
            val targetMember = members.firstOrNull { it.userId == toProfile.userId }
            val mergedMember = buildMergedMember(
                currentProfile = toProfile,
                sourceMember = sourceMember,
                targetMember = targetMember,
                group = group,
            )

            updates["groupMembers/$groupId/${toProfile.userId}"] = mergedMember.toFirebaseMap()
            updates["groupMembers/$groupId/$fromUserId"] = null
            updates["userGroups/${toProfile.userId}/$groupId"] = true
            updates["userGroups/$fromUserId/$groupId"] = null

            if (group.ownerUserId == fromUserId) {
                updates["groups/$groupId/ownerUserId"] = toProfile.userId
            }

            val expenses = database.reference.child("expenses").child(groupId).get().await().children
                .mapNotNull { it.toExpenseOrNull() }
            expenses.forEach { expense ->
                val mergedExpense = expense.mergeMemberIntoUser(
                    fromUserId = fromUserId,
                    toUserId = toProfile.userId,
                )
                if (mergedExpense != expense) {
                    updates["expenses/$groupId/${expense.expenseId}"] = mergedExpense.toFirebaseMap()
                }
            }

            val balances = database.reference.child("balances").child(groupId).get().await().children
                .associateNotNull(
                    keySelector = { it.key },
                    valueSelector = { it.childLongNullable("netPaise") },
                )
            if (balances.containsKey(fromUserId)) {
                updates["balances/$groupId"] = balances
                    .mergeMemberBalanceIntoUser(
                        fromUserId = fromUserId,
                        toUserId = toProfile.userId,
                    )
                    .mapValues { (userId, netPaise) ->
                        mapOf(
                            "userId" to userId,
                            "netPaise" to netPaise,
                        )
                    }
            }

            val dispatchSnapshot = database.reference.child("settlementDispatch").child(groupId).get().await()
            val dispatchMembers = dispatchSnapshot.child("members").children
                .mapNotNull { it.getValue(String::class.java) }
            if (dispatchMembers.contains(fromUserId)) {
                val mergedDispatchMembers = dispatchMembers.mergeUserIdReferences(
                    fromUserId = fromUserId,
                    toUserId = toProfile.userId,
                )
                updates["settlementDispatch/$groupId/members"] = mergedDispatchMembers
                updates["settlementDispatch/$groupId/memberCount"] = mergedDispatchMembers.size
            }
        }
    }

    private suspend fun loadKnownGroupIdsForUser(userId: String): Set<String> {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return emptySet()

        val groupIds = linkedSetOf<String>()
        groupIds += runCatching {
            database.reference.child("userGroups").child(normalizedUserId).get().await().children
                .mapNotNull { it.key }
        }.getOrDefault(emptyList())
        groupIds += queryOwnedGroupIds(normalizedUserId)
        groupIds += queryMemberGroupIds(normalizedUserId)
        return groupIds
    }

    private suspend fun queryOwnedGroupIds(userId: String): Set<String> {
        val snapshot = runCatching {
            database.reference
                .child("groups")
                .orderByChild("ownerUserId")
                .equalTo(userId)
                .get()
                .await()
        }.getOrNull() ?: return emptySet()

        return snapshot.children.mapNotNull { it.key }.toSet()
    }

    private suspend fun queryMemberGroupIds(userId: String): Set<String> {
        val groupIds = linkedSetOf<String>()
        groupIds += queryGroupIdsByMemberField(userId = userId, field = "role", value = Role.OWNER.name)
        groupIds += queryGroupIdsByMemberField(userId = userId, field = "role", value = Role.MEMBER.name)
        groupIds += queryGroupIdsByMemberField(userId = userId, field = "active", value = true)
        return groupIds
    }

    private suspend fun queryGroupIdsByMemberField(
        userId: String,
        field: String,
        value: String,
    ): Set<String> {
        val snapshot = runCatching {
            database.reference
                .child("groupMembers")
                .orderByChild("$userId/$field")
                .equalTo(value)
                .get()
                .await()
        }.getOrNull() ?: return emptySet()

        return snapshot.children.mapNotNull { it.key }.toSet()
    }

    private suspend fun queryGroupIdsByMemberField(
        userId: String,
        field: String,
        value: Boolean,
    ): Set<String> {
        val snapshot = runCatching {
            database.reference
                .child("groupMembers")
                .orderByChild("$userId/$field")
                .equalTo(value)
                .get()
                .await()
        }.getOrNull() ?: return emptySet()

        return snapshot.children.mapNotNull { it.key }.toSet()
    }

    private suspend fun mergeUserOwnedNode(
        rootPath: String,
        fromUserId: String,
        toUserId: String,
        updates: MutableMap<String, Any?>,
        userIdFieldName: String? = null,
    ) {
        val fromSnapshot = database.reference.child(rootPath).child(fromUserId).get().await()
        if (!fromSnapshot.exists()) return
        val toSnapshot = database.reference.child(rootPath).child(toUserId).get().await()

        fromSnapshot.children.forEach { child ->
            val key = child.key ?: return@forEach
            val payload = child.toFirebasePayload(userIdFieldName = userIdFieldName, userId = toUserId)
            updates["$rootPath/$toUserId/$key"] = payload
        }
        toSnapshot.children.forEach { child ->
            val key = child.key ?: return@forEach
            val payload = child.toFirebasePayload(userIdFieldName = userIdFieldName, userId = toUserId)
            updates["$rootPath/$toUserId/$key"] = payload
        }
        updates["$rootPath/$fromUserId"] = null
    }

    private fun buildMergedMember(
        currentProfile: UserProfile,
        sourceMember: Member,
        targetMember: Member?,
        group: com.buddingintents.letsgodutch.core.model.Group,
    ): Member {
        val mergedRole = if (
            sourceMember.role == Role.OWNER ||
            targetMember?.role == Role.OWNER ||
            group.ownerUserId == sourceMember.userId ||
            group.ownerUserId == targetMember?.userId
        ) {
            Role.OWNER
        } else {
            Role.MEMBER
        }
        val mergedJoinedAt = minOf(
            sourceMember.joinedAtEpochMs,
            targetMember?.joinedAtEpochMs ?: Long.MAX_VALUE,
        ).takeUnless { it == Long.MAX_VALUE } ?: System.currentTimeMillis()

        return Member(
            userId = currentProfile.userId,
            displayName = firstNotBlank(
                currentProfile.displayName,
                targetMember?.displayName.orEmpty(),
                sourceMember.displayName,
                "Member",
            ),
            email = firstNotBlank(
                currentProfile.email,
                targetMember?.email.orEmpty(),
                sourceMember.email,
            ),
            identifier = firstNotBlank(
                currentProfile.identifier,
                targetMember?.identifier.orEmpty(),
                sourceMember.identifier,
            ),
            photoUrl = currentProfile.photoUrl ?: targetMember?.photoUrl ?: sourceMember.photoUrl,
            joinedAtEpochMs = mergedJoinedAt,
            role = mergedRole,
            active = true,
        )
    }
}

private data class DeviceContext(
    val deviceId: String,
    val deviceModel: String,
    val country: String,
)

private data class AnonymousAccountCandidate(
    val userId: String,
    val hasGroupMembership: Boolean,
    val createdAtEpochMs: Long,
)

private fun UserProfile.toFirebaseProfileMap(): Map<String, Any> {
    val payload = mutableMapOf<String, Any>(
        "displayName" to displayName,
        "email" to email,
        "createdAtEpochMs" to createdAtEpochMs,
        "isAnonymous" to isAnonymous,
        "identifier" to identifier.ifBlank { deviceId },
        "deviceId" to deviceId,
        "deviceModel" to deviceModel,
        "country" to country,
    )
    photoUrl?.takeIf { it.isNotBlank() }?.let { payload["photoUrl"] = it }
    return payload
}

private fun DataSnapshot?.childString(key: String): String {
    return this?.child(key)?.getValue(String::class.java).orEmpty()
}

private fun DataSnapshot?.childLongNullable(key: String): Long? {
    return this?.child(key)?.getValue(Long::class.java)
        ?: this?.child(key)?.getValue(Int::class.java)?.toLong()
}

private fun DataSnapshot.toFirebasePayload(
    userIdFieldName: String? = null,
    userId: String = "",
): Any {
    val baseValue = value.toFirebaseValue()
    return if (userIdFieldName.isNullOrBlank()) {
        baseValue ?: emptyMap<String, Any>()
    } else {
        val payload = (baseValue as? Map<*, *>)
            ?.entries
            ?.associateNotNull(
                keySelector = { it.key?.toString() },
                valueSelector = { it.value.toFirebaseValue() },
            )
            ?.toMutableMap()
            ?: mutableMapOf()
        payload[userIdFieldName] = userId
        payload
    }
}

private fun Any?.toFirebaseValue(): Any? {
    return when (this) {
        is Map<*, *> -> this.entries.associateNotNull(
            keySelector = { it.key?.toString() },
            valueSelector = { it.value.toFirebaseValue() },
        )

        is List<*> -> mapNotNull { it.toFirebaseValue() }
        else -> this
    }
}

private fun firstNotBlank(vararg values: String): String {
    return values.firstOrNull { it.isNotBlank() }.orEmpty()
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

private fun FirebaseUser.canonicalEmail(): String {
    return email?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: if (isAnonymous) "" else "$uid@example.com"
}

private fun Context.currentDeviceContext(): DeviceContext {
    val androidId = runCatching {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }.getOrNull().orEmpty().trim()
    val manufacturer = Build.MANUFACTURER.orEmpty().trim()
    val model = Build.MODEL.orEmpty().trim()
    val deviceModel = listOf(manufacturer, model)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" ")
        .ifBlank { "Android Device" }

    val locale = runCatching {
        resources.configuration.locales[0]
    }.getOrNull() ?: Locale.getDefault()
    val countryCode = locale.country.orEmpty().trim()
    val country = if (countryCode.isNotBlank()) {
        Locale("", countryCode).getDisplayCountry(Locale.ENGLISH).ifBlank { countryCode.uppercase(Locale.ENGLISH) }
    } else {
        locale.displayCountry.orEmpty().trim()
    }

    return DeviceContext(
        deviceId = androidId,
        deviceModel = deviceModel,
        country = country.ifBlank { "Unknown" },
    )
}

private fun mapAnonymousSignInError(error: Throwable): Throwable {
    val errorCode = (error as? FirebaseAuthException)
        ?.errorCode
        .orEmpty()
        .uppercase()
    val normalizedMessage = (error.localizedMessage ?: error.message)
        .orEmpty()
        .lowercase()

    val disabledByConfig = errorCode.contains("OPERATION_NOT_ALLOWED") ||
        errorCode.contains("ADMIN_ONLY_OPERATION") ||
        normalizedMessage.contains("operation not allowed") ||
        normalizedMessage.contains("restricted to administrators") ||
        normalizedMessage.contains("admin_only_operation")

    if (!disabledByConfig) return error

    return IllegalStateException(
        "Anonymous sign-in is disabled for this Firebase project. " +
            "Enable it in Firebase Console > Authentication > Sign-in method > Anonymous.",
        error,
    )
}

private const val ANONYMOUS_DEVICES_NODE = "anonymousDevices"
