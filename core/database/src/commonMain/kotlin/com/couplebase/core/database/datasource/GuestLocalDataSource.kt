package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.Guest
import com.couplebase.core.model.RsvpStatus
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GuestLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val queries get() = database.guestQueries

    fun observeByCoupleId(coupleId: String): Flow<List<Guest>> =
        queries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getByCoupleId(coupleId: String): List<Guest> =
        queries.getByCoupleId(coupleId).executeAsList().map { it.toDomain() }

    fun getById(id: String): Guest? =
        queries.getById(id).executeAsOneOrNull()?.toDomain()

    fun getByRsvpStatus(coupleId: String, status: String): List<Guest> =
        queries.getByRsvpStatus(coupleId, status).executeAsList().map { it.toDomain() }

    fun searchByName(coupleId: String, query: String): List<Guest> =
        queries.searchByName(coupleId, query).executeAsList().map { it.toDomain() }

    fun insert(guest: Guest) {
        queries.insert(
            id = guest.id,
            couple_id = guest.coupleId,
            name = guest.name,
            email = guest.email,
            phone = guest.phone,
            rsvp_status = guest.rsvpStatus.name,
            meal_preference = guest.mealPreference,
            table_number = guest.tableNumber?.toLong(),
            has_plus_one = if (guest.hasPlusOne) 1L else 0L,
            notes = guest.notes,
            updated_at = guest.updatedAt,
            sync_status = guest.syncStatus.name,
            is_deleted = if (guest.isDeleted) 1L else 0L,
        )
    }

    fun softDelete(id: String, updatedAt: String) {
        queries.softDelete(updated_at = updatedAt, id = id)
    }

    fun countByCoupleId(coupleId: String): Long =
        queries.countByCoupleId(coupleId).executeAsOne()

    fun countByRsvpStatus(coupleId: String, status: String): Long =
        queries.countByRsvpStatus(coupleId, status).executeAsOne()
}

private fun com.couplebase.core.database.Guest.toDomain(): Guest =
    Guest(
        id = id,
        coupleId = couple_id,
        name = name,
        email = email,
        phone = phone,
        rsvpStatus = runCatching { RsvpStatus.valueOf(rsvp_status) }.getOrDefault(RsvpStatus.PENDING),
        mealPreference = meal_preference,
        tableNumber = table_number?.toInt(),
        hasPlusOne = has_plus_one != 0L,
        notes = notes,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
