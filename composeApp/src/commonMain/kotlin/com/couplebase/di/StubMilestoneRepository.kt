package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.core.model.Milestone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubMilestoneRepository : MilestoneRepository {

    private val milestones = MutableStateFlow<List<Milestone>>(emptyList())

    override fun milestonesFlow(coupleId: String): Flow<List<Milestone>> =
        milestones.map { list ->
            list.filter { it.coupleId == coupleId && !it.isDeleted }
                .sortedBy { it.date }
        }

    override suspend fun getById(id: String): Result<Milestone?> =
        Result.Success(milestones.value.find { it.id == id })

    override suspend fun upsert(milestone: Milestone): Result<Milestone> {
        milestones.update { list ->
            list.filter { it.id != milestone.id } + milestone
        }
        return Result.Success(milestone)
    }

    override suspend fun delete(id: String): Result<Unit> {
        milestones.update { list -> list.filter { it.id != id } }
        return Result.Success(Unit)
    }
}
