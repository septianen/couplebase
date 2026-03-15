package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.GoalMilestone
import com.couplebase.core.model.LifeGoal
import kotlinx.coroutines.flow.Flow

interface LifeGoalRepository {
    fun goalsFlow(coupleId: String): Flow<List<LifeGoal>>
    suspend fun getGoalById(id: String): Result<LifeGoal?>
    suspend fun upsertGoal(goal: LifeGoal): Result<LifeGoal>
    suspend fun deleteGoal(id: String): Result<Unit>
    fun goalMilestonesFlow(goalId: String): Flow<List<GoalMilestone>>
    suspend fun upsertMilestone(milestone: GoalMilestone): Result<GoalMilestone>
    suspend fun toggleMilestone(id: String, isCompleted: Boolean): Result<Unit>
    suspend fun deleteMilestone(id: String): Result<Unit>
    suspend fun getGoalProgress(goalId: String): Result<Pair<Long, Long>>
}
