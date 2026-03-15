package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.MilestoneDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class MilestoneRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["milestones"]

    suspend fun getByCoupleId(coupleId: String): List<MilestoneDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("date", Order.ASCENDING)
        }.decodeList()

    suspend fun upsert(dto: MilestoneDto): MilestoneDto =
        table.upsert(dto) { select() }.decodeSingle()

    suspend fun delete(id: String) {
        table.update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
    }
}
