package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.SyncStatus
import com.couplebase.core.model.Vendor
import com.couplebase.core.model.VendorPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VendorLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val vendorQueries get() = database.vendorQueries
    private val paymentQueries get() = database.vendorPaymentQueries

    // --- Vendors ---

    fun observeVendors(coupleId: String): Flow<List<Vendor>> =
        vendorQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getVendorById(id: String): Vendor? =
        vendorQueries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insertVendor(vendor: Vendor) {
        vendorQueries.insert(
            id = vendor.id,
            couple_id = vendor.coupleId,
            name = vendor.name,
            category = vendor.category,
            phone = vendor.phone,
            email = vendor.email,
            website = vendor.website,
            total_cost = vendor.totalCost,
            notes = vendor.notes,
            updated_at = vendor.updatedAt,
            sync_status = vendor.syncStatus.name,
            is_deleted = if (vendor.isDeleted) 1L else 0L,
        )
    }

    fun softDeleteVendor(id: String, updatedAt: String) {
        vendorQueries.softDelete(updated_at = updatedAt, id = id)
    }

    // --- Payments ---

    fun observePayments(vendorId: String): Flow<List<VendorPayment>> =
        paymentQueries.getByVendorId(vendorId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getPaymentById(id: String): VendorPayment? =
        paymentQueries.getById(id).executeAsOneOrNull()?.toDomain()

    fun insertPayment(payment: VendorPayment) {
        paymentQueries.insert(
            id = payment.id,
            vendor_id = payment.vendorId,
            couple_id = payment.coupleId,
            description = payment.description,
            amount = payment.amount,
            due_date = payment.dueDate,
            is_paid = if (payment.isPaid) 1L else 0L,
            updated_at = payment.updatedAt,
            sync_status = payment.syncStatus.name,
            is_deleted = if (payment.isDeleted) 1L else 0L,
        )
    }

    fun markPaymentPaid(id: String, updatedAt: String) {
        paymentQueries.markPaid(updated_at = updatedAt, id = id)
    }

    fun softDeletePayment(id: String, updatedAt: String) {
        paymentQueries.softDelete(updated_at = updatedAt, id = id)
    }

    fun getUpcomingPayments(coupleId: String, limit: Long): List<VendorPayment> =
        paymentQueries.getUpcoming(coupleId, limit).executeAsList().map { it.toDomain() }

    fun getTotalPaidByVendor(vendorId: String): Double =
        paymentQueries.getTotalPaidByVendor(vendorId).executeAsOne()
}

private fun com.couplebase.core.database.Vendor.toDomain(): Vendor =
    Vendor(
        id = id,
        coupleId = couple_id,
        name = name,
        category = category,
        phone = phone,
        email = email,
        website = website,
        totalCost = total_cost,
        notes = notes,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Vendor_payment.toDomain(): VendorPayment =
    VendorPayment(
        id = id,
        vendorId = vendor_id,
        coupleId = couple_id,
        description = description,
        amount = amount,
        dueDate = due_date,
        isPaid = is_paid != 0L,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
