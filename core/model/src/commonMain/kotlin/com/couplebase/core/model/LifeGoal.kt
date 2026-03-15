package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LifeGoal(
    val id: String,
    val coupleId: String,
    val title: String,
    val description: String? = null,
    val targetDate: String? = null,
    val progress: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
