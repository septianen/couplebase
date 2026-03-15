package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.datastore.PreferencesDataStore
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class StubAuthRepository(
    private val preferencesDataStore: PreferencesDataStore,
) : AuthRepository {
    private val currentUser = MutableStateFlow<User?>(null)

    override fun currentUserFlow(): Flow<User?> = currentUser

    override suspend fun signUp(email: String, password: String, fullName: String): Result<User> {
        val user = User(
            id = "stub-user-id",
            fullName = fullName,
            email = email,
            createdAt = "",
            updatedAt = "",
        )
        currentUser.value = user
        preferencesDataStore.updatePreferences { it.copy(userId = user.id) }
        return Result.Success(user)
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        val user = User(
            id = "stub-user-id",
            fullName = "Test User",
            email = email,
            createdAt = "",
            updatedAt = "",
        )
        currentUser.value = user
        preferencesDataStore.updatePreferences { it.copy(userId = user.id) }
        return Result.Success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        currentUser.value = null
        preferencesDataStore.updatePreferences { it.copy(userId = null, coupleId = null) }
        return Result.Success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User?> = Result.Success(currentUser.value)

    override suspend fun isLoggedIn(): Boolean {
        if (currentUser.value != null) return true
        val prefs = preferencesDataStore.getPreferences()
        val savedUserId = prefs.userId
        if (savedUserId != null) {
            currentUser.value = User(
                id = savedUserId,
                fullName = "Test User",
                email = "test@couplebase.com",
                createdAt = "",
                updatedAt = "",
            )
            return true
        }
        return false
    }
}
