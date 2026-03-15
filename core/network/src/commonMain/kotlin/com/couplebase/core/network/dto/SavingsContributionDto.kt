package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavingsContributionDto(
    val id: String,
    @SerialName("goal_id") val goalId: String,
    @SerialName("couple_id") val coupleId: String,
    val amount: Double,
    val date: String,
    val note: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
