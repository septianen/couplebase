package com.couplebase.feature.finance.budget.usecase

import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

class GetMonthlyBudgetUseCase(
    private val repository: FinanceRepository,
) {
    operator fun invoke(coupleId: String, yearMonth: String): Flow<List<MonthlyBudget>> =
        repository.monthlyBudgetsFlow(coupleId, yearMonth)
}
