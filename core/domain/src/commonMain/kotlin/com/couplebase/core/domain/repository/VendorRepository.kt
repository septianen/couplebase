package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.Vendor
import com.couplebase.core.model.VendorPayment
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    fun vendorsFlow(coupleId: String): Flow<List<Vendor>>
    suspend fun getVendorById(id: String): Result<Vendor?>
    suspend fun upsertVendor(vendor: Vendor): Result<Vendor>
    suspend fun deleteVendor(id: String): Result<Unit>
    fun paymentsFlow(vendorId: String): Flow<List<VendorPayment>>
    suspend fun upsertPayment(payment: VendorPayment): Result<VendorPayment>
    suspend fun markPaymentPaid(id: String): Result<Unit>
    suspend fun deletePayment(id: String): Result<Unit>
    suspend fun getUpcomingPayments(coupleId: String, limit: Long): Result<List<VendorPayment>>
}
