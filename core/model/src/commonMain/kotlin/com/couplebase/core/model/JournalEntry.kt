package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    val id: String,
    val coupleId: String,
    val authorId: String,
    val body: String,
    val isShared: Boolean = false,
    val date: String,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
