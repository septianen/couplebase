package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoupleDto(
    val id: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("partner1_id") val partner1Id: String,
    @SerialName("partner2_id") val partner2Id: String? = null,
    @SerialName("couple_name") val coupleName: String? = null,
    @SerialName("wedding_date") val weddingDate: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("together_since") val togetherSince: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
