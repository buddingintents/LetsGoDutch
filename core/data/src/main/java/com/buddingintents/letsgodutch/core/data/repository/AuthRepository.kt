package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    fun observeRecentAnonymousDisplayNames(): Flow<List<String>>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserProfile>
    suspend fun signInAnonymously(displayName: String): Result<UserProfile>
    suspend fun updateDisplayName(displayName: String): Result<UserProfile>
    suspend fun signOut()
}
