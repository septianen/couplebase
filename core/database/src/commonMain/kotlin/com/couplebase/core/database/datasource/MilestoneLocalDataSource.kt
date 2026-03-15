package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.Milestone
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MilestoneLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.milestoneQueries

    fun observeMilestones(coupleId: String): Flow<List<Milestone>> =
        queries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getById(id: String): Milestone? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insert(milestone: Milestone) {
        queries.insert(
            id = milestone.id,
            couple_id = milestone.coupleId,
            title = milestone.title,
            date = milestone.date,
            description = milestone.description,
            icon = milestone.icon,
            sort_order = milestone.sortOrder.toLong(),
            updated_at = milestone.updatedAt,
            sync_status = milestone.syncStatus.name,
            is_deleted = if (milestone.isDeleted) 1L else 0L,
        )
    }

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }
}

private fun com.couplebase.core.database.Milestone.toDomain(): Milestone =
    Milestone(
        id = id,
        coupleId = couple_id,
        title = title,
        date = date,
        description = description,
        icon = icon,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
