package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val coupleId: String? = null,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
