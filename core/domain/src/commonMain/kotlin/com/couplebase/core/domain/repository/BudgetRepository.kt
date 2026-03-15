package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun categoriesFlow(coupleId: String): Flow<List<BudgetCategory>>
    fun expensesFlow(coupleId: String): Flow<List<Expense>>
    suspend fun upsertCategory(category: BudgetCategory): Result<BudgetCategory>
    suspend fun deleteCategory(id: String): Result<Unit>
    suspend fun upsertExpense(expense: Expense): Result<Expense>
    suspend fun deleteExpense(id: String): Result<Unit>
    suspend fun getExpensesByCategory(coupleId: String, categoryId: String): Result<List<Expense>>
    suspend fun getWeddingExpenses(coupleId: String): Result<List<Expense>>
    suspend fun getExpensesByDateRange(coupleId: String, start: String, end: String): Result<List<Expense>>
    suspend fun getTotalSpent(coupleId: String): Result<Double>
    suspend fun getTotalAllocated(coupleId: String): Result<Double>
}
