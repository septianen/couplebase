package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.TimelineBlockDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class TimelineRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["timeline_blocks"]

    suspend fun getByCoupleId(coupleId: String): List<TimelineBlockDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("start_time", Order.ASCENDING)
        }.decodeList()

    suspend fun getById(id: String): TimelineBlockDto? =
        table.select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()

    suspend fun upsert(dto: TimelineBlockDto): TimelineBlockDto =
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
