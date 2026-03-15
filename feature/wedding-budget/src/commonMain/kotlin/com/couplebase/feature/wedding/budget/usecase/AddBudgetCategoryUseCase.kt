package com.couplebase.feature.wedding.budget.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.BudgetCategory

class AddBudgetCategoryUseCase(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(category: BudgetCategory): Result<BudgetCategory> =
        repository.upsertCategory(category)
}
