package com.couplebase.feature.finance.savings.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.SavingsContribution

class AddContributionUseCase(
    private val repository: FinanceRepository,
) {
    suspend operator fun invoke(contribution: SavingsContribution): Result<SavingsContribution> =
        repository.addContribution(contribution)
}
