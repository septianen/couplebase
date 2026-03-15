package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncStatus {
    SYNCED,
    PENDING,
    CONFLICT
}
