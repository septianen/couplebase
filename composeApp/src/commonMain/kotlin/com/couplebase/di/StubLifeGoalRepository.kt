package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.model.GoalMilestone
import com.couplebase.core.model.LifeGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubLifeGoalRepository : LifeGoalRepository {

    private val goals = MutableStateFlow<List<LifeGoal>>(emptyList())
    private val milestones = MutableStateFlow<List<GoalMilestone>>(emptyList())

    override fun goalsFlow(coupleId: String): Flow<List<LifeGoal>> =
        goals.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override suspend fun getGoalById(id: String): Result<LifeGoal?> =
        Result.Success(goals.value.find { it.id == id })

    override suspend fun upsertGoal(goal: LifeGoal): Result<LifeGoal> {
        goals.update { list -> list.filter { it.id != goal.id } + goal }
        return Result.Success(goal)
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        goals.update { list -> list.filter { it.id != id } }
        milestones.update { list -> list.filter { it.goalId != id } }
        return Result.Success(Unit)
    }

    override fun goalMilestonesFlow(goalId: String): Flow<List<GoalMilestone>> =
        milestones.map { list ->
            list.filter { it.goalId == goalId && !it.isDeleted }
                .sortedBy { it.sortOrder }
        }

    override suspend fun upsertMilestone(milestone: GoalMilestone): Result<GoalMilestone> {
        milestones.update { list -> list.filter { it.id != milestone.id } + milestone }
        return Result.Success(milestone)
    }

    override suspend fun toggleMilestone(id: String, isCompleted: Boolean): Result<Unit> {
        milestones.update { list ->
            list.map { if (it.id == id) it.copy(isCompleted = isCompleted) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteMilestone(id: String): Result<Unit> {
        milestones.update { list -> list.filter { it.id != id } }
        return Result.Success(Unit)
    }

    override suspend fun getGoalProgress(goalId: String): Result<Pair<Long, Long>> {
        val all = milestones.value.filter { it.goalId == goalId && !it.isDeleted }
        val completed = all.count { it.isCompleted }.toLong()
        return Result.Success(completed to all.size.toLong())
    }
}
