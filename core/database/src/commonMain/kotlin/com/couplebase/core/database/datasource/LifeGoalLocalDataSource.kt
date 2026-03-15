package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.GoalMilestone
import com.couplebase.core.model.LifeGoal
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LifeGoalLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val goalQueries get() = database.lifeGoalQueries
    private val milestoneQueries get() = database.goalMilestoneQueries

    // --- Goals ---

    fun observeGoals(coupleId: String): Flow<List<LifeGoal>> =
        goalQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getGoalById(id: String): LifeGoal? =
        goalQueries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insertGoal(goal: LifeGoal) {
        goalQueries.insert(
            id = goal.id,
            couple_id = goal.coupleId,
            title = goal.title,
            description = goal.description,
            target_date = goal.targetDate,
            progress = goal.progress.toLong(),
            updated_at = goal.updatedAt,
            sync_status = goal.syncStatus.name,
            is_deleted = if (goal.isDeleted) 1L else 0L,
        )
    }

    fun updateProgress(id: String, progress: Int, updatedAt: String) {
        goalQueries.updateProgress(progress = progress.toLong(), updated_at = updatedAt, id = id)
    }

    fun softDeleteGoal(id: String, updatedAt: String) {
        goalQueries.softDelete(updated_at = updatedAt, id = id)
    }

    // --- Goal Milestones ---

    fun observeMilestones(goalId: String): Flow<List<GoalMilestone>> =
        milestoneQueries.getByGoalId(goalId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun insertMilestone(milestone: GoalMilestone) {
        milestoneQueries.insert(
            id = milestone.id,
            goal_id = milestone.goalId,
            couple_id = milestone.coupleId,
            title = milestone.title,
            is_completed = if (milestone.isCompleted) 1L else 0L,
            sort_order = milestone.sortOrder.toLong(),
            updated_at = milestone.updatedAt,
            sync_status = milestone.syncStatus.name,
            is_deleted = if (milestone.isDeleted) 1L else 0L,
        )
    }

    fun toggleMilestone(id: String, isCompleted: Boolean, updatedAt: String) {
        milestoneQueries.toggleCompleted(
            is_completed = if (isCompleted) 1L else 0L,
            updated_at = updatedAt,
            id = id,
        )
    }

    fun softDeleteMilestone(id: String, updatedAt: String) {
        milestoneQueries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getGoalProgress(goalId: String): Pair<Long, Long> {
        val total = milestoneQueries.countByGoalId(goalId).executeAsOne()
        val completed = milestoneQueries.countCompletedByGoalId(goalId).executeAsOne()
        return completed to total
    }
}

private fun com.couplebase.core.database.Life_goal.toDomain(): LifeGoal =
    LifeGoal(
        id = id,
        coupleId = couple_id,
        title = title,
        description = description,
        targetDate = target_date,
        progress = progress.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Goal_milestone.toDomain(): GoalMilestone =
    GoalMilestone(
        id = id,
        goalId = goal_id,
        coupleId = couple_id,
        title = title,
        isCompleted = is_completed != 0L,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
