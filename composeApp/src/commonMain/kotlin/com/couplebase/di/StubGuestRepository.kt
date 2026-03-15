package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.model.Guest
import com.couplebase.core.model.RsvpStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubGuestRepository : GuestRepository {

    private val guests = MutableStateFlow<List<Guest>>(emptyList())

    override fun guestsFlow(coupleId: String): Flow<List<Guest>> =
        guests.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted } }

    override suspend fun getById(id: String): Result<Guest?> =
        Result.Success(guests.value.find { it.id == id })

    override suspend fun getByRsvpStatus(coupleId: String, status: String): Result<List<Guest>> =
        Result.Success(guests.value.filter { it.coupleId == coupleId && it.rsvpStatus.name == status && !it.isDeleted })

    override suspend fun search(coupleId: String, query: String): Result<List<Guest>> =
        Result.Success(guests.value.filter {
            it.coupleId == coupleId && !it.isDeleted && it.name.contains(query, ignoreCase = true)
        })

    override suspend fun upsert(guest: Guest): Result<Guest> {
        guests.update { list ->
            val idx = list.indexOfFirst { it.id == guest.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, guest) }
            else list + guest
        }
        return Result.Success(guest)
    }

    override suspend fun delete(id: String): Result<Unit> {
        guests.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override suspend fun getGuestCount(coupleId: String): Result<Long> =
        Result.Success(guests.value.count { it.coupleId == coupleId && !it.isDeleted }.toLong())

    override suspend fun getCountByRsvpStatus(coupleId: String, status: String): Result<Long> =
        Result.Success(guests.value.count { it.coupleId == coupleId && it.rsvpStatus.name == status && !it.isDeleted }.toLong())
}
