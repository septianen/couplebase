package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.TimelineRepository
import com.couplebase.core.model.TimelineBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubTimelineRepository : TimelineRepository {

    private val blocks = MutableStateFlow<List<TimelineBlock>>(emptyList())

    override fun timelineFlow(coupleId: String): Flow<List<TimelineBlock>> =
        blocks.map { list ->
            list.filter { it.coupleId == coupleId && !it.isDeleted }
                .sortedBy { it.startTime }
        }

    override suspend fun getById(id: String): Result<TimelineBlock?> =
        Result.Success(blocks.value.find { it.id == id })

    override suspend fun upsert(block: TimelineBlock): Result<TimelineBlock> {
        blocks.update { list ->
            val filtered = list.filter { it.id != block.id }
            filtered + block
        }
        return Result.Success(block)
    }

    override suspend fun delete(id: String): Result<Unit> {
        blocks.update { list -> list.filter { it.id != id } }
        return Result.Success(Unit)
    }
}
