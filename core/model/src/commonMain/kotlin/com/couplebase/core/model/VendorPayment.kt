package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VendorPayment(
    val id: String,
    val vendorId: String,
    val coupleId: String,
    val description: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean = false,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
