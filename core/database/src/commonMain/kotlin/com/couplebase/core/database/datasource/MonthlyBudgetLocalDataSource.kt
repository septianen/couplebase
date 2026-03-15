package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.MonthlyBudget
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MonthlyBudgetLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.monthlyBudgetQueries

    fun observeBudgets(coupleId: String, yearMonth: String): Flow<List<MonthlyBudget>> =
        queries.getByCoupleIdAndMonth(coupleId, yearMonth)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getById(id: String): MonthlyBudget? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insert(budget: MonthlyBudget) {
        queries.insert(
            id = budget.id,
            couple_id = budget.coupleId,
            year_month = budget.yearMonth,
            category = budget.category,
            limit_amount = budget.limitAmount,
            income_amount = budget.incomeAmount,
            updated_at = budget.updatedAt,
            sync_status = budget.syncStatus.name,
            is_deleted = if (budget.isDeleted) 1L else 0L,
        )
    }

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getTotalIncome(coupleId: String, yearMonth: String): Double =
        queries.getTotalIncome(coupleId, yearMonth).executeAsOne()

    fun getTotalLimit(coupleId: String, yearMonth: String): Double =
        queries.getTotalLimit(coupleId, yearMonth).executeAsOne()
}

private fun com.couplebase.core.database.Monthly_budget.toDomain(): MonthlyBudget =
    MonthlyBudget(
        id = id,
        coupleId = couple_id,
        yearMonth = year_month,
        category = category,
        limitAmount = limit_amount,
        incomeAmount = income_amount,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
