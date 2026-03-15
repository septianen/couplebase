package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalPhotoDto(
    val id: String,
    @SerialName("entry_id") val entryId: String,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
