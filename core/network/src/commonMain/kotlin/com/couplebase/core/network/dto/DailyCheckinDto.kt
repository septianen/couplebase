package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyCheckinDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("user_id") val userId: String,
    val date: String,
    val mood: String,
    val reflection: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)
