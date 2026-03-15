package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.ChecklistItem
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    fun checklistFlow(coupleId: String): Flow<List<ChecklistItem>>
    suspend fun getById(id: String): Result<ChecklistItem?>
    suspend fun getByCategory(coupleId: String, category: String): Result<List<ChecklistItem>>
    suspend fun getByAssignedTo(coupleId: String, assignedTo: String): Result<List<ChecklistItem>>
    suspend fun getCompleted(coupleId: String): Result<List<ChecklistItem>>
    suspend fun upsert(item: ChecklistItem): Result<ChecklistItem>
    suspend fun toggleCompleted(id: String, isCompleted: Boolean): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    suspend fun getProgress(coupleId: String): Result<Pair<Long, Long>>
}
