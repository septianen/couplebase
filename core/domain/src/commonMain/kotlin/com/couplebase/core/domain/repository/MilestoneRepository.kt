package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.Milestone
import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {
    fun milestonesFlow(coupleId: String): Flow<List<Milestone>>
    suspend fun getById(id: String): Result<Milestone?>
    suspend fun upsert(milestone: Milestone): Result<Milestone>
    suspend fun delete(id: String): Result<Unit>
}
