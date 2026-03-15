package com.couplebase.feature.finance.expenses.usecase

import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetExpensesUseCase(
    private val repository: BudgetRepository,
) {
    operator fun invoke(
        coupleId: String,
        month: String? = null,
        categoryId: String? = null,
        search: String? = null,
    ): Flow<List<Expense>> =
        repository.expensesFlow(coupleId).map { expenses ->
            expenses
                .filter { !it.isWeddingExpense }
                .let { list ->
                    if (month != null) list.filter { it.date.startsWith(month) } else list
                }
                .let { list ->
                    if (categoryId != null) list.filter { it.categoryId == categoryId } else list
                }
                .let { list ->
                    if (!search.isNullOrBlank()) list.filter {
                        it.description.contains(search, ignoreCase = true)
                    } else list
                }
        }
}
