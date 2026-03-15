package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LifeGoalDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val title: String,
    val description: String? = null,
    @SerialName("target_date") val targetDate: String? = null,
    val progress: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
