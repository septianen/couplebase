package com.couplebase.feature.finance.savings.usecase

import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

class GetSavingsGoalsUseCase(
    private val repository: FinanceRepository,
) {
    operator fun invoke(coupleId: String): Flow<List<SavingsGoal>> =
        repository.savingsGoalsFlow(coupleId)
}
