package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SavingsGoal(
    val id: String,
    val coupleId: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null,
    val icon: String? = null,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
