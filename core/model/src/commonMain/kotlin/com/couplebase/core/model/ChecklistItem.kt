package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistItem(
    val id: String,
    val coupleId: String,
    val title: String,
    val category: String? = null,
    val dueDate: String? = null,
    val assignedTo: AssignedTo? = null,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
