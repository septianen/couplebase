package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.TimelineBlock
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    fun timelineFlow(coupleId: String): Flow<List<TimelineBlock>>
    suspend fun getById(id: String): Result<TimelineBlock?>
    suspend fun upsert(block: TimelineBlock): Result<TimelineBlock>
    suspend fun delete(id: String): Result<Unit>
}
