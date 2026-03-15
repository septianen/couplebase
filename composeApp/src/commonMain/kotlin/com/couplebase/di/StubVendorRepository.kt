package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.core.model.Vendor
import com.couplebase.core.model.VendorPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubVendorRepository : VendorRepository {

    private val vendors = MutableStateFlow<List<Vendor>>(emptyList())
    private val payments = MutableStateFlow<List<VendorPayment>>(emptyList())

    override fun vendorsFlow(coupleId: String): Flow<List<Vendor>> =
        vendors.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override suspend fun getVendorById(id: String): Result<Vendor?> =
        Result.Success(vendors.value.find { it.id == id })

    override suspend fun upsertVendor(vendor: Vendor): Result<Vendor> {
        vendors.update { list ->
            val filtered = list.filter { it.id != vendor.id }
            filtered + vendor
        }
        return Result.Success(vendor)
    }

    override suspend fun deleteVendor(id: String): Result<Unit> {
        vendors.update { list -> list.filter { it.id != id } }
        payments.update { list -> list.filter { it.vendorId != id } }
        return Result.Success(Unit)
    }

    override fun paymentsFlow(vendorId: String): Flow<List<VendorPayment>> =
        payments.map { list ->
            list.filter { it.vendorId == vendorId && !it.isDeleted }
                .sortedBy { it.dueDate }
        }

    override suspend fun upsertPayment(payment: VendorPayment): Result<VendorPayment> {
        payments.update { list ->
            val filtered = list.filter { it.id != payment.id }
            filtered + payment
        }
        return Result.Success(payment)
    }

    override suspend fun markPaymentPaid(id: String): Result<Unit> {
        payments.update { list ->
            list.map { if (it.id == id) it.copy(isPaid = true) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun deletePayment(id: String): Result<Unit> {
        payments.update { list -> list.filter { it.id != id } }
        return Result.Success(Unit)
    }

    override suspend fun getUpcomingPayments(coupleId: String, limit: Long): Result<List<VendorPayment>> {
        val upcoming = payments.value
            .filter { it.coupleId == coupleId && !it.isPaid && !it.isDeleted }
            .sortedBy { it.dueDate }
            .take(limit.toInt())
        return Result.Success(upcoming)
    }
}
