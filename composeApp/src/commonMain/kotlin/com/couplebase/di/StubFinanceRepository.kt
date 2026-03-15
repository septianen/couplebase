package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.MonthlyBudget
import com.couplebase.core.model.SavingsContribution
import com.couplebase.core.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubFinanceRepository : FinanceRepository {

    private val budgets = MutableStateFlow<List<MonthlyBudget>>(emptyList())
    private val savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    private val contributions = MutableStateFlow<List<SavingsContribution>>(emptyList())

    override fun monthlyBudgetsFlow(coupleId: String, yearMonth: String): Flow<List<MonthlyBudget>> =
        budgets.map { list ->
            list.filter { it.coupleId == coupleId && it.yearMonth == yearMonth && !it.isDeleted }
        }

    override suspend fun upsertMonthlyBudget(budget: MonthlyBudget): Result<MonthlyBudget> {
        budgets.update { list ->
            val idx = list.indexOfFirst { it.id == budget.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, budget) }
            else list + budget
        }
        return Result.Success(budget)
    }

    override suspend fun deleteMonthlyBudget(id: String): Result<Unit> {
        budgets.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override suspend fun getTotalIncome(coupleId: String, yearMonth: String): Result<Double> =
        Result.Success(
            budgets.value
                .filter { it.coupleId == coupleId && it.yearMonth == yearMonth && !it.isDeleted }
                .sumOf { it.incomeAmount }
        )

    override suspend fun getTotalLimit(coupleId: String, yearMonth: String): Result<Double> =
        Result.Success(
            budgets.value
                .filter { it.coupleId == coupleId && it.yearMonth == yearMonth && !it.isDeleted }
                .sumOf { it.limitAmount }
        )

    override fun savingsGoalsFlow(coupleId: String): Flow<List<SavingsGoal>> =
        savingsGoals.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override suspend fun upsertSavingsGoal(goal: SavingsGoal): Result<SavingsGoal> {
        savingsGoals.update { list ->
            val idx = list.indexOfFirst { it.id == goal.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, goal) }
            else list + goal
        }
        return Result.Success(goal)
    }

    override suspend fun deleteSavingsGoal(id: String): Result<Unit> {
        savingsGoals.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override fun contributionsFlow(goalId: String): Flow<List<SavingsContribution>> =
        contributions.map { list -> list.filter { it.goalId == goalId && !it.isDeleted } }

    override suspend fun addContribution(contribution: SavingsContribution): Result<SavingsContribution> {
        contributions.update { it + contribution }
        return Result.Success(contribution)
    }

    override suspend fun deleteContribution(id: String): Result<Unit> {
        contributions.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }
}
