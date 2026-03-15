package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VendorPaymentDto(
    val id: String,
    @SerialName("vendor_id") val vendorId: String,
    @SerialName("couple_id") val coupleId: String,
    val description: String,
    val amount: Double,
    @SerialName("due_date") val dueDate: String,
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
