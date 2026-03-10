package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaidBy {
    ME,
    PARTNER,
    SPLIT
}
