package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.Guest
import kotlinx.coroutines.flow.Flow

interface GuestRepository {
    fun guestsFlow(coupleId: String): Flow<List<Guest>>
    suspend fun getById(id: String): Result<Guest?>
    suspend fun getByRsvpStatus(coupleId: String, status: String): Result<List<Guest>>
    suspend fun search(coupleId: String, query: String): Result<List<Guest>>
    suspend fun upsert(guest: Guest): Result<Guest>
    suspend fun delete(id: String): Result<Unit>
    suspend fun getGuestCount(coupleId: String): Result<Long>
    suspend fun getCountByRsvpStatus(coupleId: String, status: String): Result<Long>
}
