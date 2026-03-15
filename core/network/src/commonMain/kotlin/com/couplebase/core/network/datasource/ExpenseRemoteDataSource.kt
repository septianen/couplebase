package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.ExpenseDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class ExpenseRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["expenses"]

    suspend fun getByCoupleId(coupleId: String): List<ExpenseDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun getByDateRange(coupleId: String, start: String, end: String): List<ExpenseDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            filter { gte("date", start) }
            filter { lte("date", end) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun upsert(dto: ExpenseDto): ExpenseDto =
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
