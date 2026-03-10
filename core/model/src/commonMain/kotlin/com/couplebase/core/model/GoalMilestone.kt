package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class GoalMilestone(
    val id: String,
    val goalId: String,
    val coupleId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
