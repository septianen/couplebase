package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VendorDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val name: String,
    val category: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    @SerialName("total_cost") val totalCost: Double = 0.0,
    val notes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
