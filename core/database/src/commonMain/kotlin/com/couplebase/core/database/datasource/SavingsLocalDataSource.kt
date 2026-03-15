package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.SavingsContribution
import com.couplebase.core.model.SavingsGoal
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val goalQueries get() = database.savingsGoalQueries
    private val contributionQueries get() = database.savingsContributionQueries

    // --- Savings Goals ---

    fun observeGoals(coupleId: String): Flow<List<SavingsGoal>> =
        goalQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getGoalById(id: String): SavingsGoal? =
        goalQueries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insertGoal(goal: SavingsGoal) {
        goalQueries.insert(
            id = goal.id,
            couple_id = goal.coupleId,
            title = goal.title,
            target_amount = goal.targetAmount,
            current_amount = goal.currentAmount,
            target_date = goal.targetDate,
            icon = goal.icon,
            updated_at = goal.updatedAt,
            sync_status = goal.syncStatus.name,
            is_deleted = if (goal.isDeleted) 1L else 0L,
        )
    }

    fun updateCurrentAmount(id: String, amount: Double, updatedAt: String) {
        goalQueries.updateCurrentAmount(
            current_amount = amount,
            updated_at = updatedAt,
            id = id,
        )
    }

    fun softDeleteGoal(id: String, updatedAt: String) {
        goalQueries.softDelete(updated_at = updatedAt, id = id)
    }

    // --- Contributions ---

    fun observeContributions(goalId: String): Flow<List<SavingsContribution>> =
        contributionQueries.getByGoalId(goalId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun insertContribution(contribution: SavingsContribution) {
        contributionQueries.insert(
            id = contribution.id,
            goal_id = contribution.goalId,
            couple_id = contribution.coupleId,
            amount = contribution.amount,
            date = contribution.date,
            note = contribution.note,
            updated_at = contribution.updatedAt,
            sync_status = contribution.syncStatus.name,
            is_deleted = if (contribution.isDeleted) 1L else 0L,
        )
    }

    fun softDeleteContribution(id: String, updatedAt: String) {
        contributionQueries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getTotalContributions(goalId: String): Double =
        contributionQueries.getTotalByGoalId(goalId).executeAsOne()
}

private fun com.couplebase.core.database.Savings_goal.toDomain(): SavingsGoal =
    SavingsGoal(
        id = id,
        coupleId = couple_id,
        title = title,
        targetAmount = target_amount,
        currentAmount = current_amount,
        targetDate = target_date,
        icon = icon,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Savings_contribution.toDomain(): SavingsContribution =
    SavingsContribution(
        id = id,
        goalId = goal_id,
        coupleId = couple_id,
        amount = amount,
        date = date,
        note = note,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
