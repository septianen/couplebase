package com.couplebase.feature.wedding.budget.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.Expense

class GetCategoryExpensesUseCase(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(coupleId: String, categoryId: String): Result<List<Expense>> =
        repository.getExpensesByCategory(coupleId, categoryId)
}
