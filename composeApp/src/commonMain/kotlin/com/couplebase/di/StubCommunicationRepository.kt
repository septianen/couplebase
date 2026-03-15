package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.CommunicationRepository
import com.couplebase.core.model.DailyCheckin
import com.couplebase.core.model.JournalEntry
import com.couplebase.core.model.JournalPhoto
import com.couplebase.core.model.SharedNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StubCommunicationRepository : CommunicationRepository {

    private val notes = MutableStateFlow<List<SharedNote>>(emptyList())
    private val entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    private val photos = MutableStateFlow<List<JournalPhoto>>(emptyList())
    private val checkins = MutableStateFlow<List<DailyCheckin>>(emptyList())

    // --- Notes ---

    override fun notesFlow(coupleId: String): Flow<List<SharedNote>> =
        notes.map { list ->
            list.filter { it.coupleId == coupleId && !it.isDeleted }
                .sortedWith(compareByDescending<SharedNote> { it.isPinned }.thenByDescending { it.updatedAt })
        }

    override suspend fun upsertNote(note: SharedNote): Result<SharedNote> {
        notes.update { list ->
            val idx = list.indexOfFirst { it.id == note.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, note) } else list + note
        }
        return Result.Success(note)
    }

    override suspend fun toggleNotePin(id: String, isPinned: Boolean): Result<Unit> {
        notes.update { list -> list.map { if (it.id == id) it.copy(isPinned = isPinned) else it } }
        return Result.Success(Unit)
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        notes.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    // --- Journal ---

    override fun journalFlow(coupleId: String): Flow<List<JournalEntry>> =
        entries.map { list -> list.filter { it.coupleId == coupleId && !it.isDeleted }.sortedByDescending { it.date } }

    override suspend fun getJournalByDate(coupleId: String, date: String): Result<List<JournalEntry>> =
        Result.Success(entries.value.filter { it.coupleId == coupleId && it.date == date && !it.isDeleted })

    override suspend fun getOnThisDay(coupleId: String, monthDay: String, currentYear: String): Result<List<JournalEntry>> =
        Result.Success(
            entries.value.filter {
                it.coupleId == coupleId && !it.isDeleted &&
                    it.date.substring(5, 10) == monthDay &&
                    it.date.substring(0, 4) != currentYear
            }
        )

    override suspend fun upsertJournalEntry(entry: JournalEntry): Result<JournalEntry> {
        entries.update { list ->
            val idx = list.indexOfFirst { it.id == entry.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, entry) } else list + entry
        }
        return Result.Success(entry)
    }

    override suspend fun deleteJournalEntry(id: String): Result<Unit> {
        entries.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    override fun journalPhotosFlow(entryId: String): Flow<List<JournalPhoto>> =
        photos.map { list -> list.filter { it.entryId == entryId && !it.isDeleted } }

    override suspend fun addJournalPhoto(photo: JournalPhoto): Result<JournalPhoto> {
        photos.update { it + photo }
        return Result.Success(photo)
    }

    override suspend fun deleteJournalPhoto(id: String): Result<Unit> {
        photos.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        return Result.Success(Unit)
    }

    // --- Check-in ---

    override suspend fun getCheckin(coupleId: String, userId: String, date: String): Result<DailyCheckin?> =
        Result.Success(checkins.value.find { it.coupleId == coupleId && it.userId == userId && it.date == date })

    override suspend fun getPartnerCheckin(coupleId: String, userId: String, date: String): Result<DailyCheckin?> =
        Result.Success(checkins.value.find { it.coupleId == coupleId && it.userId != userId && it.date == date })

    override suspend fun getWeeklyMoods(coupleId: String, startDate: String, endDate: String): Result<List<DailyCheckin>> =
        Result.Success(checkins.value.filter { it.coupleId == coupleId && it.date in startDate..endDate })

    override suspend fun upsertCheckin(checkin: DailyCheckin): Result<DailyCheckin> {
        checkins.update { list ->
            val idx = list.indexOfFirst { it.coupleId == checkin.coupleId && it.userId == checkin.userId && it.date == checkin.date }
            if (idx >= 0) list.toMutableList().apply { set(idx, checkin) } else list + checkin
        }
        return Result.Success(checkin)
    }
}
