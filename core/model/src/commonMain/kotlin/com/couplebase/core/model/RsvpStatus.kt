package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class RsvpStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
