package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoalMilestoneDto(
    val id: String,
    @SerialName("goal_id") val goalId: String,
    @SerialName("couple_id") val coupleId: String,
    val title: String,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
