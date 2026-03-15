package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.MonthlyBudgetDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class MonthlyBudgetRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["monthly_budgets"]

    suspend fun getByCoupleIdAndMonth(coupleId: String, yearMonth: String): List<MonthlyBudgetDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("year_month", yearMonth) }
            filter { eq("is_deleted", false) }
            order("category", Order.ASCENDING)
        }.decodeList()

    suspend fun upsert(dto: MonthlyBudgetDto): MonthlyBudgetDto =
        table.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun delete(id: String) {
        table.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
