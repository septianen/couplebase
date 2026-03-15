package com.couplebase.core.datastore

import kotlinx.coroutines.flow.Flow

interface PreferencesDataStore {
    fun preferencesFlow(): Flow<UserPreferences>
    suspend fun getPreferences(): UserPreferences
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
    suspend fun clear()
}
