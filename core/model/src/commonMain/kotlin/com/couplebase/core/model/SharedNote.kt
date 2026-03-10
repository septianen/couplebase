package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SharedNote(
    val id: String,
    val coupleId: String,
    val title: String,
    val body: String = "",
    val isPinned: Boolean = false,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
