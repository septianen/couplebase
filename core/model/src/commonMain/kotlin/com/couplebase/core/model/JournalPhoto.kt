package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class JournalPhoto(
    val id: String,
    val entryId: String,
    val coupleId: String,
    val photoUrl: String,
    val sortOrder: Int = 0,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
