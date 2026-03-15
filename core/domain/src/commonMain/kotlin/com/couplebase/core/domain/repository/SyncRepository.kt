package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun pendingCountFlow(): Flow<Long>
    suspend fun syncAll(): Result<Unit>
    suspend fun syncTable(tableName: String): Result<Unit>
    suspend fun getPendingCount(): Result<Long>
}
