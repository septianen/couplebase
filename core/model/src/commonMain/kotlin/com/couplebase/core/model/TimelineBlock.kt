package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TimelineBlock(
    val id: String,
    val coupleId: String,
    val title: String,
    val startTime: String,
    val durationMinutes: Int,
    val location: String? = null,
    val description: String? = null,
    val assignedPeople: String? = null,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
