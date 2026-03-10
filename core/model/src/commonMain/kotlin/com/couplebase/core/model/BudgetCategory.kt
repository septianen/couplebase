package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class BudgetCategory(
    val id: String,
    val coupleId: String,
    val name: String,
    val allocatedAmount: Double = 0.0,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
