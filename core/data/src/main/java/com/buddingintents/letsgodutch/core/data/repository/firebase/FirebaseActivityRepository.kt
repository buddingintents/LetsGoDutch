package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.util.Log
import com.buddingintents.letsgodutch.core.data.repository.ActivityRepository
import com.buddingintents.letsgodutch.core.model.GroupActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseActivityRepository(
    database: FirebaseDatabase,
) : ActivityRepository {
    private val root = database.reference

    override fun observeGroupActivities(groupId: String): Flow<List<GroupActivity>> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val activitiesRef = root.child("activities").child(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = snapshot.children
                    .mapNotNull { it.toGroupActivityOrNull() }
                    .sortedByDescending { it.createdAtEpochMs }
                trySend(activities)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(
                    "FirebaseActivityRepo",
                    "observeGroupActivities cancelled: code=${error.code}, message=${error.message}",
                )
                trySend(emptyList())
                close()
            }
        }

        activitiesRef.addValueEventListener(listener)
        awaitClose {
            activitiesRef.removeEventListener(listener)
        }
    }
}
