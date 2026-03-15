package com.couplebase.core.datastore

import com.couplebase.core.model.ThemeMode
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userId: String? = null,
    val coupleId: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val notificationsEnabled: Boolean = true,
    val dailyCheckinReminder: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val onboardingCompleted: Boolean = false,
)
