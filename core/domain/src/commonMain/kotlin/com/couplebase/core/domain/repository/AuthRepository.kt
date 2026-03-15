package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUserFlow(): Flow<User?>
    suspend fun signUp(email: String, password: String, fullName: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun isLoggedIn(): Boolean
}
