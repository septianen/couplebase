package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AssignedTo {
    ME,
    PARTNER,
    BOTH
}
