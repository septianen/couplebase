package com.couplebase.core.database.datasource

import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.AssignedTo
import com.couplebase.core.model.ChecklistItem
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers

class ChecklistLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.checklistItemQueries

    fun observeByCoupleId(coupleId: String): Flow<List<ChecklistItem>> =
        queries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getByCoupleId(coupleId: String): List<ChecklistItem> =
        queries.getByCoupleId(coupleId).executeAsList().map { it.toDomain() }

    fun getById(id: String): ChecklistItem? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun getByAssignedTo(coupleId: String, assignedTo: String): List<ChecklistItem> =
        queries.getByAssignedTo(coupleId, assignedTo).executeAsList().map { it.toDomain() }

    fun getCompleted(coupleId: String): List<ChecklistItem> =
        queries.getCompleted(coupleId).executeAsList().map { it.toDomain() }

    fun getByCategory(coupleId: String, category: String): List<ChecklistItem> =
        queries.getByCategory(coupleId, category).executeAsList().map { it.toDomain() }

    fun getBySyncStatus(status: String): List<ChecklistItem> =
        queries.getBySyncStatus(status).executeAsList().map { it.toDomain() }

    fun insert(item: ChecklistItem) {
        queries.insert(
            id = item.id,
            couple_id = item.coupleId,
            title = item.title,
            category = item.category,
            due_date = item.dueDate,
            assigned_to = item.assignedTo?.name,
            is_completed = if (item.isCompleted) 1L else 0L,
            sort_order = item.sortOrder.toLong(),
            updated_at = item.updatedAt,
            sync_status = item.syncStatus.name,
            is_deleted = if (item.isDeleted) 1L else 0L,
        )
    }

    fun toggleCompleted(id: String, isCompleted: Boolean, updatedAt: String) {
        queries.toggleCompleted(
            is_completed = if (isCompleted) 1L else 0L,
            updated_at = updatedAt,
            id = id,
        )
    }

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }

    fun updateSyncStatus(id: String, status: SyncStatus, updatedAt: String) {
        queries.updateSyncStatus(
            sync_status = status.name,
            updated_at = updatedAt,
            id = id,
        )
    }

    fun countByCoupleId(coupleId: String): Long =
        queries.countByCoupleId(coupleId).executeAsOne()

    fun countCompletedByCoupleId(coupleId: String): Long =
        queries.countCompletedByCoupleId(coupleId).executeAsOne()
}

private fun com.couplebase.core.database.Checklist_item.toDomain(): ChecklistItem =
    ChecklistItem(
        id = id,
        coupleId = couple_id,
        title = title,
        category = category,
        dueDate = due_date,
        assignedTo = assigned_to?.let { runCatching { AssignedTo.valueOf(it) }.getOrNull() },
        isCompleted = is_completed != 0L,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
