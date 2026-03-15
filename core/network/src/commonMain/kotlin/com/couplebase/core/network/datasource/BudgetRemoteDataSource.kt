package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.BudgetCategoryDto
import com.couplebase.core.network.dto.ExpenseDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class BudgetRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val categoryTable get() = client.postgrest["budget_categories"]
    private val expenseTable get() = client.postgrest["expenses"]

    // --- Budget Categories ---

    suspend fun getCategories(coupleId: String): List<BudgetCategoryDto> =
        categoryTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("sort_order", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertCategory(dto: BudgetCategoryDto): BudgetCategoryDto =
        categoryTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun deleteCategory(id: String) {
        categoryTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }

    // --- Expenses ---

    suspend fun getWeddingExpenses(coupleId: String): List<ExpenseDto> =
        expenseTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_wedding_expense", true) }
            filter { eq("is_deleted", false) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun getExpensesByCategory(coupleId: String, categoryId: String): List<ExpenseDto> =
        expenseTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("category_id", categoryId) }
            filter { eq("is_deleted", false) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun upsertExpense(dto: ExpenseDto): ExpenseDto =
        expenseTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun deleteExpense(id: String) {
        expenseTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
