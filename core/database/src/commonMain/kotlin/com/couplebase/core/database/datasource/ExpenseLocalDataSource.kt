package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.Expense
import com.couplebase.core.model.PaidBy
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.expenseQueries

    fun observeAllExpenses(coupleId: String): Flow<List<Expense>> =
        queries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun observeByDateRange(coupleId: String, start: String, end: String): Flow<List<Expense>> =
        queries.getByDateRange(coupleId, start, end)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getById(id: String): Expense? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insert(expense: Expense) {
        queries.insert(
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

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getTotalByCouple(coupleId: String): Double =
        queries.getTotalByCoupleId(coupleId).executeAsOne()
}

private fun com.couplebase.core.database.Expense.toDomain(): Expense =
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
