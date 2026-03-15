package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val coupleId: String,
    val categoryId: String? = null,
    val description: String,
    val amount: Double,
    val paidBy: PaidBy? = null,
    val receiptUrl: String? = null,
    val date: String,
    val isWeddingExpense: Boolean = false,
    val updatedAt: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
