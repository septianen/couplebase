package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyCheckin(
    val id: String,
    val coupleId: String,
    val userId: String,
    val date: String,
    val mood: Mood,
    val reflection: String? = null,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
