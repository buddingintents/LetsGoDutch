package com.buddingintents.letsgodutch.core.data.repository

import com.buddingintents.letsgodutch.core.model.GroupActivity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeGroupActivities(groupId: String): Flow<List<GroupActivity>>
}
