package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MonthlyBudget(
    val id: String,
    val coupleId: String,
    val yearMonth: String,
    val category: String,
    val limitAmount: Double,
    val incomeAmount: Double = 0.0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
