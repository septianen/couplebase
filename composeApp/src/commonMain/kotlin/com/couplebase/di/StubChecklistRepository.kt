package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.model.AssignedTo
import com.couplebase.core.model.ChecklistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubChecklistRepository : ChecklistRepository {

    private val items = MutableStateFlow<List<ChecklistItem>>(emptyList())

    override fun checklistFlow(coupleId: String): Flow<List<ChecklistItem>> =
        items.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override suspend fun getById(id: String): Result<ChecklistItem?> =
        Result.Success(items.value.find { it.id == id })

    override suspend fun getByCategory(coupleId: String, category: String): Result<List<ChecklistItem>> =
        Result.Success(items.value.filter { it.coupleId == coupleId && it.category == category && !it.isDeleted })

    override suspend fun getByAssignedTo(coupleId: String, assignedTo: String): Result<List<ChecklistItem>> =
        Result.Success(items.value.filter { it.coupleId == coupleId && it.assignedTo?.name == assignedTo && !it.isDeleted })

    override suspend fun getCompleted(coupleId: String): Result<List<ChecklistItem>> =
        Result.Success(items.value.filter { it.coupleId == coupleId && it.isCompleted && !it.isDeleted })

    override suspend fun upsert(item: ChecklistItem): Result<ChecklistItem> {
        items.update { list ->
            val existing = list.indexOfFirst { it.id == item.id }
            if (existing >= 0) {
                list.toMutableList().apply { set(existing, item) }
            } else {
                list + item
            }
        }
        return Result.Success(item)
    }

    override suspend fun toggleCompleted(id: String, isCompleted: Boolean): Result<Unit> {
        items.update { list ->
            list.map { if (it.id == id) it.copy(isCompleted = isCompleted) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        items.update { list ->
            list.map { if (it.id == id) it.copy(isDeleted = true) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun getProgress(coupleId: String): Result<Pair<Long, Long>> {
        val active = items.value.filter { it.coupleId == coupleId && !it.isDeleted }
        val completed = active.count { it.isCompleted }.toLong()
        val total = active.size.toLong()
        return Result.Success(completed to total)
    }
}
