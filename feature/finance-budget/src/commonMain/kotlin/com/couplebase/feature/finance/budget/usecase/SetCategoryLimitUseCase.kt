package com.couplebase.feature.finance.budget.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.MonthlyBudget

class SetCategoryLimitUseCase(
    private val repository: FinanceRepository,
) {
    suspend operator fun invoke(budget: MonthlyBudget): Result<MonthlyBudget> =
        repository.upsertMonthlyBudget(budget)
}
