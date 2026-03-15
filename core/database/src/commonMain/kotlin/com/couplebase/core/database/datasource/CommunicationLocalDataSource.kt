package com.couplebase.core.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.couplebase.core.database.CouplebaseDatabase
import com.couplebase.core.model.DailyCheckin
import com.couplebase.core.model.JournalEntry
import com.couplebase.core.model.JournalPhoto
import com.couplebase.core.model.Mood
import com.couplebase.core.model.SharedNote
import com.couplebase.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CommunicationLocalDataSource(
    private val database: CouplebaseDatabase,
) {
    private val checkinQueries get() = database.dailyCheckinQueries
    private val noteQueries get() = database.sharedNoteQueries
    private val journalQueries get() = database.journalEntryQueries
    private val photoQueries get() = database.journalPhotoQueries

    // --- Daily Check-in ---

    fun getCheckin(coupleId: String, userId: String, date: String): DailyCheckin? =
        checkinQueries.getByUserAndDate(coupleId, userId, date)
            .executeAsOneOrNull()?.toCheckinDomain()

    fun getPartnerCheckin(coupleId: String, userId: String, date: String): DailyCheckin? =
        checkinQueries.getPartnerCheckin(coupleId, userId, date)
            .executeAsOneOrNull()?.toCheckinDomain()

    fun getWeeklyMoods(coupleId: String, startDate: String, endDate: String): List<DailyCheckin> =
        checkinQueries.getWeeklyMoods(coupleId, startDate, endDate)
            .executeAsList().map { it.toCheckinDomain() }

    fun insertCheckin(checkin: DailyCheckin) {
        checkinQueries.insert(
            id = checkin.id,
            couple_id = checkin.coupleId,
            user_id = checkin.userId,
            date = checkin.date,
            mood = checkin.mood.name,
            reflection = checkin.reflection,
            updated_at = checkin.updatedAt,
            sync_status = checkin.syncStatus.name,
        )
    }

    // --- Shared Notes ---

    fun observeNotes(coupleId: String): Flow<List<SharedNote>> =
        noteQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toNoteDomain() } }

    fun insertNote(note: SharedNote) {
        noteQueries.insert(
            id = note.id,
            couple_id = note.coupleId,
            title = note.title,
            body = note.body,
            is_pinned = if (note.isPinned) 1L else 0L,
            updated_at = note.updatedAt,
            sync_status = note.syncStatus.name,
            is_deleted = if (note.isDeleted) 1L else 0L,
        )
    }

    fun toggleNotePin(id: String, isPinned: Boolean, updatedAt: String) {
        noteQueries.togglePinned(
            is_pinned = if (isPinned) 1L else 0L,
            updated_at = updatedAt,
            id = id,
        )
    }

    fun softDeleteNote(id: String, updatedAt: String) {
        noteQueries.softDelete(updated_at = updatedAt, id = id)
    }

    // --- Journal ---

    fun observeJournal(coupleId: String): Flow<List<JournalEntry>> =
        journalQueries.getByCoupleId(coupleId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toJournalDomain() } }

    fun getJournalByDate(coupleId: String, date: String): List<JournalEntry> =
        journalQueries.getByDate(coupleId, date).executeAsList().map { it.toJournalDomain() }

    fun getOnThisDay(coupleId: String, monthDay: String, currentYear: String): List<JournalEntry> =
        journalQueries.getOnThisDay(coupleId, monthDay, currentYear)
            .executeAsList().map { it.toJournalDomain() }

    fun insertJournalEntry(entry: JournalEntry) {
        journalQueries.insert(
            id = entry.id,
            couple_id = entry.coupleId,
            author_id = entry.authorId,
            body = entry.body,
            is_shared = if (entry.isShared) 1L else 0L,
            date = entry.date,
            updated_at = entry.updatedAt,
            sync_status = entry.syncStatus.name,
            is_deleted = if (entry.isDeleted) 1L else 0L,
        )
    }

    fun softDeleteJournalEntry(id: String, updatedAt: String) {
        journalQueries.softDelete(updated_at = updatedAt, id = id)
    }

    // --- Journal Photos ---

    fun observePhotos(entryId: String): Flow<List<JournalPhoto>> =
        photoQueries.getByEntryId(entryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toPhotoDomain() } }

    fun insertPhoto(photo: JournalPhoto) {
        photoQueries.insert(
            id = photo.id,
            entry_id = photo.entryId,
            couple_id = photo.coupleId,
            photo_url = photo.photoUrl,
            sort_order = photo.sortOrder.toLong(),
            updated_at = photo.updatedAt,
            sync_status = photo.syncStatus.name,
            is_deleted = if (photo.isDeleted) 1L else 0L,
        )
    }

    fun softDeletePhoto(id: String, updatedAt: String) {
        photoQueries.softDelete(updated_at = updatedAt, id = id)
    }
}

private fun com.couplebase.core.database.Daily_checkin.toCheckinDomain(): DailyCheckin =
    DailyCheckin(
        id = id,
        coupleId = couple_id,
        userId = user_id,
        date = date,
        mood = runCatching { Mood.valueOf(mood) }.getOrDefault(Mood.NEUTRAL),
        reflection = reflection,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
    )

private fun com.couplebase.core.database.Shared_note.toNoteDomain(): SharedNote =
    SharedNote(
        id = id,
        coupleId = couple_id,
        title = title,
        body = body,
        isPinned = is_pinned != 0L,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Journal_entry.toJournalDomain(): JournalEntry =
    JournalEntry(
        id = id,
        coupleId = couple_id,
        authorId = author_id,
        body = body,
        isShared = is_shared != 0L,
        date = date,
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )

private fun com.couplebase.core.database.Journal_photo.toPhotoDomain(): JournalPhoto =
    JournalPhoto(
        id = id,
        entryId = entry_id,
        coupleId = couple_id,
        photoUrl = photo_url,
        sortOrder = sort_order.toInt(),
        updatedAt = updated_at,
        syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.PENDING),
        isDeleted = is_deleted != 0L,
    )
