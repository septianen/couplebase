package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalEntryDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("author_id") val authorId: String,
    val body: String,
    @SerialName("is_shared") val isShared: Boolean = false,
    val date: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
