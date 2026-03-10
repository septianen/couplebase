package com.couplebase.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Mood(val emoji: String) {
    HAPPY("\uD83D\uDE0A"),
    NEUTRAL("\uD83D\uDE10"),
    SAD("\uD83D\uDE14"),
    IN_LOVE("\uD83D\uDE0D"),
    ANGRY("\uD83D\uDE24")
}
