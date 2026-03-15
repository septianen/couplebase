package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    val id: String,
    val coupleId: String,
    val name: String,
    val category: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val totalCost: Double = 0.0,
    val notes: String? = null,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
