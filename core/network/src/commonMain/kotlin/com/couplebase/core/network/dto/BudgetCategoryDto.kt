package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BudgetCategoryDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val name: String,
    @SerialName("allocated_amount") val allocatedAmount: Double = 0.0,
    val icon: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
