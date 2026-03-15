package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistItemDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val title: String,
    val category: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
