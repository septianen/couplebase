package com.couplebase.feature.wedding.budget.usecase

import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetOverview(
    val categories: List<BudgetCategory>,
    val expenses: List<Expense>,
    val totalAllocated: Double,
    val totalSpent: Double,
) {
    val remaining: Double get() = totalAllocated - totalSpent
    val isOverBudget: Boolean get() = totalSpent > totalAllocated
    val spentPercent: Float get() = if (totalAllocated > 0) (totalSpent / totalAllocated).toFloat().coerceIn(0f, 1.5f) else 0f
}

data class CategorySummary(
    val category: BudgetCategory,
    val spent: Double,
) {
    val remaining: Double get() = category.allocatedAmount - spent
    val isOverBudget: Boolean get() = spent > category.allocatedAmount
    val spentPercent: Float get() = if (category.allocatedAmount > 0) (spent / category.allocatedAmount).toFloat().coerceIn(0f, 1.5f) else 0f
}

class GetBudgetOverviewUseCase(
    private val repository: BudgetRepository,
) {
    operator fun invoke(coupleId: String): Flow<BudgetOverview> =
        combine(
            repository.categoriesFlow(coupleId),
            repository.expensesFlow(coupleId),
        ) { categories, expenses ->
            val totalAllocated = categories.sumOf { it.allocatedAmount }
            val totalSpent = expenses.sumOf { it.amount }
            BudgetOverview(
                categories = categories,
                expenses = expenses,
                totalAllocated = totalAllocated,
                totalSpent = totalSpent,
            )
        }
}
