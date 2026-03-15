package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuestDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("rsvp_status") val rsvpStatus: String = "PENDING",
    @SerialName("meal_preference") val mealPreference: String? = null,
    @SerialName("table_number") val tableNumber: Int? = null,
    @SerialName("has_plus_one") val hasPlusOne: Boolean = false,
    val notes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
