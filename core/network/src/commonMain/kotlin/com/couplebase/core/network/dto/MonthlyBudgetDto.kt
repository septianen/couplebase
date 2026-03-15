package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonthlyBudgetDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("year_month") val yearMonth: String,
    val category: String,
    @SerialName("limit_amount") val limitAmount: Double,
    @SerialName("income_amount") val incomeAmount: Double = 0.0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
