package com.couplebase.feature.wedding.budget.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.Expense

class AddExpenseUseCase(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(expense: Expense): Result<Expense> =
        repository.upsertExpense(expense)
}
