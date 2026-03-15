package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Temporary stub for AuthRepository until Supabase integration is wired.
 * Will be replaced by the real implementation in a future task.
 */
class StubAuthRepository : AuthRepository {
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
        return Result.Success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        currentUser.value = null
        return Result.Success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User?> = Result.Success(currentUser.value)

    override suspend fun isLoggedIn(): Boolean = currentUser.value != null
}
