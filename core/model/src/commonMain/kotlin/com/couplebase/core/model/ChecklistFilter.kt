package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChecklistFilter {
    ALL,
    MINE,
    PARTNER,
    COMPLETED
}
