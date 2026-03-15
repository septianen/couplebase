package com.couplebase.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class PreferencesDataStoreImpl(
    private val storage: PlatformStorage,
) : PreferencesDataStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private val _preferencesFlow = MutableStateFlow(loadPreferences())

    override fun preferencesFlow(): Flow<UserPreferences> = _preferencesFlow.asStateFlow()

    override suspend fun getPreferences(): UserPreferences = _preferencesFlow.value

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        mutex.withLock {
            val updated = transform(_preferencesFlow.value)
            savePreferences(updated)
            _preferencesFlow.value = updated
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            val default = UserPreferences()
            savePreferences(default)
            _preferencesFlow.value = default
        }
    }

    private fun loadPreferences(): UserPreferences {
        val raw = storage.getString(PREFS_KEY) ?: return UserPreferences()
        return try {
            json.decodeFromString<UserPreferences>(raw)
        } catch (_: Exception) {
            UserPreferences()
        }
    }

    private fun savePreferences(prefs: UserPreferences) {
        val raw = json.encodeToString(UserPreferences.serializer(), prefs)
        storage.putString(PREFS_KEY, raw)
    }

    companion object {
        private const val PREFS_KEY = "user_preferences"
    }
}
