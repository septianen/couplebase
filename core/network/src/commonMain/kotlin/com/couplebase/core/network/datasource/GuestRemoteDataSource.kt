package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.GuestDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class GuestRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val table get() = client.postgrest["guests"]

    suspend fun getByCoupleId(coupleId: String): List<GuestDto> =
        table.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun getById(id: String): GuestDto? =
        table.select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()

    suspend fun upsert(dto: GuestDto): GuestDto =
        table.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun upsertBatch(guests: List<GuestDto>) {
        if (guests.isEmpty()) return
        table.upsert(guests)
    }

    suspend fun delete(id: String) {
        table.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
