package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SavingsContribution(
    val id: String,
    val goalId: String,
    val coupleId: String,
    val amount: Double,
    val date: String,
    val note: String? = null,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
