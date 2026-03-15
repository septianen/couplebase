package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.ChecklistItemDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ChecklistRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["checklist_items"]

    suspend fun getByCoupleId(coupleId: String): List<ChecklistItemDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("sort_order", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
        }.decodeList()

    suspend fun getById(id: String): ChecklistItemDto? =
        table.select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()

    suspend fun upsert(dto: ChecklistItemDto): ChecklistItemDto =
        table.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun upsertBatch(items: List<ChecklistItemDto>) {
        if (items.isEmpty()) return
        table.upsert(items)
    }

    suspend fun delete(id: String) {
        table.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
