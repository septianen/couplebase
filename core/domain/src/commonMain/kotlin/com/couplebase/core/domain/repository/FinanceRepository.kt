package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.MonthlyBudget
import com.couplebase.core.model.SavingsContribution
import com.couplebase.core.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun monthlyBudgetsFlow(coupleId: String, yearMonth: String): Flow<List<MonthlyBudget>>
    suspend fun upsertMonthlyBudget(budget: MonthlyBudget): Result<MonthlyBudget>
    suspend fun deleteMonthlyBudget(id: String): Result<Unit>
    suspend fun getTotalIncome(coupleId: String, yearMonth: String): Result<Double>
    suspend fun getTotalLimit(coupleId: String, yearMonth: String): Result<Double>

    fun savingsGoalsFlow(coupleId: String): Flow<List<SavingsGoal>>
    suspend fun upsertSavingsGoal(goal: SavingsGoal): Result<SavingsGoal>
    suspend fun deleteSavingsGoal(id: String): Result<Unit>

    fun contributionsFlow(goalId: String): Flow<List<SavingsContribution>>
    suspend fun addContribution(contribution: SavingsContribution): Result<SavingsContribution>
    suspend fun deleteContribution(id: String): Result<Unit>
}
