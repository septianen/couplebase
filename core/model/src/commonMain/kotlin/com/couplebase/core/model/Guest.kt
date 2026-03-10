package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Guest(
    val id: String,
    val coupleId: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val rsvpStatus: RsvpStatus = RsvpStatus.PENDING,
    val mealPreference: String? = null,
    val tableNumber: Int? = null,
    val hasPlusOne: Boolean = false,
    val notes: String? = null,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
