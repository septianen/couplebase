package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Milestone(
    val id: String,
    val coupleId: String,
    val title: String,
    val date: String,
    val description: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
