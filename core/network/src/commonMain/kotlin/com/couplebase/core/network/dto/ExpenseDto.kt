package com.couplebase.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("category_id") val categoryId: String? = null,
    val description: String,
    val amount: Double,
    @SerialName("paid_by") val paidBy: String? = null,
    @SerialName("receipt_url") val receiptUrl: String? = null,
    val date: String,
    @SerialName("is_wedding_expense") val isWeddingExpense: Boolean = false,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)
