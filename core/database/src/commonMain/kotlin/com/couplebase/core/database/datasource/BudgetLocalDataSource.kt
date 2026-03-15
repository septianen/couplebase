package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import com.couplebase.core.model.PaidBy
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val categoryQueries get() = database.budgetCategoryQueries
    private val expenseQueries get() = database.expenseQueries

    // --- Budget Categories ---

    fun observeCategories(coupleId: String): Flow<List<BudgetCategory>> =
        categoryQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toCategoryDomain() } }

    fun getCategoryById(id: String): BudgetCategory? =
        categoryQueries.getById(id).executeAsOneOrNull()?.toCategoryDomain()

    fun insertCategory(category: BudgetCategory) {
        categoryQueries.insert(
            id = category.id,
            couple_id = category.coupleId,
            name = category.name,
            allocated_amount = category.allocatedAmount,
            icon = category.icon,
            sort_order = category.sortOrder.toLong(),
            updated_at = category.updatedAt,
            sync_status = category.syncStatus.name,
            is_deleted = if (category.isDeleted) 1L else 0L,
        )
    }

    fun softDeleteCategory(id: String, updatedAt: String) {
        categoryQueries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getTotalAllocated(coupleId: String): Double =
        categoryQueries.getTotalAllocated(coupleId).executeAsOne()

    // --- Expenses ---

    fun observeExpenses(coupleId: String): Flow<List<Expense>> =
        expenseQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toExpenseDomain() } }

    fun observeWeddingExpenses(coupleId: String): Flow<List<Expense>> =
        expenseQueries.getWeddingExpenses(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toExpenseDomain() } }

    fun getExpensesByCategory(coupleId: String, categoryId: String): List<Expense> =
        expenseQueries.getByCategory(coupleId, categoryId).executeAsList().map { it.toExpenseDomain() }

    fun insertExpense(expense: Expense) {
        expenseQueries.insert(
            id = expense.id,
            couple_id = expense.coupleId,
            category_id = expense.categoryId,
            description = expense.description,
            amount = expense.amount,
            paid_by = expense.paidBy?.name,
            receipt_url = expense.receiptUrl,
            date = expense.date,
            is_wedding_expense = if (expense.isWeddingExpense) 1L else 0L,
            updated_at = expense.updatedAt,
            sync_status = expense.syncStatus.name,
            is_deleted = if (expense.isDeleted) 1L else 0L,
        )
    }

    fun softDeleteExpense(id: String, updatedAt: String) {
        expenseQueries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getTotalSpent(coupleId: String): Double =
        expenseQueries.getTotalWeddingByCoupleId(coupleId).executeAsOne()

    fun getTotalByCategory(coupleId: String, categoryId: String): Double =
        expenseQueries.getTotalByCategory(coupleId, categoryId).executeAsOne()
}

private fun com.couplebase.core.database.Budget_category.toCategoryDomain(): BudgetCategory =
    BudgetCategory(
        id = id,
        coupleId = couple_id,
        name = name,
        allocatedAmount = allocated_amount,
        icon = icon,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Expense.toExpenseDomain(): Expense =
    Expense(
        id = id,
        coupleId = couple_id,
        categoryId = category_id,
        description = description,
        amount = amount,
        paidBy = paid_by?.let { runCatching { PaidBy.valueOf(it) }.getOrNull() },
        receiptUrl = receipt_url,
        date = date,
        isWeddingExpense = is_wedding_expense != 0L,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
