package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MilestoneDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val title: String,
    val date: String,
    val description: String? = null,
    val icon: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
