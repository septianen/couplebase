package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Couple(
    val id: String,
    val inviteCode: String,
    val partner1Id: String,
    val partner2Id: String? = null,
    val coupleName: String? = null,
    val weddingDate: String? = null,
    val photoUrl: String? = null,
    val togetherSince: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
