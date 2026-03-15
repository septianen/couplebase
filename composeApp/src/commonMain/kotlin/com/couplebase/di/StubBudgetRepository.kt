package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubBudgetRepository : BudgetRepository {

    private val categories = MutableStateFlow<List<BudgetCategory>>(emptyList())
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    override fun categoriesFlow(coupleId: String): Flow<List<BudgetCategory>> =
        categories.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override fun expensesFlow(coupleId: String): Flow<List<Expense>> =
        expenses.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted && it.isWeddingExpense } }

    override suspend fun upsertCategory(category: BudgetCategory): Result<BudgetCategory> {
        categories.update { list ->
            val idx = list.indexOfFirst { it.id == category.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, category) }
            else list + category
        }
        return Result.Success(category)
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        categories.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override suspend fun upsertExpense(expense: Expense): Result<Expense> {
        expenses.update { list ->
            val idx = list.indexOfFirst { it.id == expense.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, expense) }
            else list + expense
        }
        return Result.Success(expense)
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        expenses.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override suspend fun getExpensesByCategory(coupleId: String, categoryId: String): Result<List<Expense>> =
        Result.Success(expenses.value.filter { it.coupleId == coupleId && it.categoryId == categoryId && !it.isDeleted })

    override suspend fun getWeddingExpenses(coupleId: String): Result<List<Expense>> =
        Result.Success(expenses.value.filter { it.coupleId == coupleId && it.isWeddingExpense && !it.isDeleted })

    override suspend fun getExpensesByDateRange(coupleId: String, start: String, end: String): Result<List<Expense>> =
        Result.Success(expenses.value.filter { it.coupleId == coupleId && !it.isDeleted && it.date in start..end })

    override suspend fun getTotalSpent(coupleId: String): Result<Double> =
        Result.Success(expenses.value.filter { it.coupleId == coupleId && it.isWeddingExpense && !it.isDeleted }.sumOf { it.amount })

    override suspend fun getTotalAllocated(coupleId: String): Result<Double> =
        Result.Success(categories.value.filter { it.coupleId == coupleId && !it.isDeleted }.sumOf { it.allocatedAmount })
}
