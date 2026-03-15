package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.VendorDto
import com.couplebase.core.network.dto.VendorPaymentDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class VendorRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val vendorTable get() = client.postgrest["vendors"]
    private val paymentTable get() = client.postgrest["vendor_payments"]

    // --- Vendors ---

    suspend fun getByCoupleId(coupleId: String): List<VendorDto> =
        vendorTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun getById(id: String): VendorDto? =
        vendorTable.select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()

    suspend fun upsertVendor(dto: VendorDto): VendorDto =
        vendorTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun deleteVendor(id: String) {
        vendorTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }

    // --- Payments ---

    suspend fun getPaymentsByVendor(vendorId: String): List<VendorPaymentDto> =
        paymentTable.select {
            filter { eq("vendor_id", vendorId) }
            filter { eq("is_deleted", false) }
            order("due_date", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertPayment(dto: VendorPaymentDto): VendorPaymentDto =
        paymentTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun markPaymentPaid(id: String) {
        paymentTable.update({
            set("is_paid", true)
        }) {
            filter { eq("id", id) }
        }
    }

    suspend fun deletePayment(id: String) {
        paymentTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
