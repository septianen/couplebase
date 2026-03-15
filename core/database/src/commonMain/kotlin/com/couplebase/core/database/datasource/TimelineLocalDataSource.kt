package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.SyncStatus
import com.couplebase.core.model.TimelineBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TimelineLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.timelineBlockQueries

    fun observeBlocks(coupleId: String): Flow<List<TimelineBlock>> =
        queries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getById(id: String): TimelineBlock? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insert(block: TimelineBlock) {
        queries.insert(
            id = block.id,
            couple_id = block.coupleId,
            title = block.title,
            start_time = block.startTime,
            duration_minutes = block.durationMinutes.toLong(),
            location = block.location,
            description = block.description,
            assigned_people = block.assignedPeople,
            sort_order = block.sortOrder.toLong(),
            updated_at = block.updatedAt,
            sync_status = block.syncStatus.name,
            is_deleted = if (block.isDeleted) 1L else 0L,
        )
    }

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }
}

private fun com.couplebase.core.database.Timeline_block.toDomain(): TimelineBlock =
    TimelineBlock(
        id = id,
        coupleId = couple_id,
        title = title,
        startTime = start_time,
        durationMinutes = duration_minutes.toInt(),
        location = location,
        description = description,
        assignedPeople = assigned_people,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
