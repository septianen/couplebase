package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedNoteDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val title: String,
    val body: String = "",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
