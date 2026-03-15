package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.DailyCheckin
import com.couplebase.core.model.JournalEntry
import com.couplebase.core.model.JournalPhoto
import com.couplebase.core.model.SharedNote
import kotlinx.coroutines.flow.Flow

interface CommunicationRepository {
    // Shared Notes
    fun notesFlow(coupleId: String): Flow<List<SharedNote>>
    suspend fun upsertNote(note: SharedNote): Result<SharedNote>
    suspend fun toggleNotePin(id: String, isPinned: Boolean): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>

    // Journal
    fun journalFlow(coupleId: String): Flow<List<JournalEntry>>
    suspend fun getJournalByDate(coupleId: String, date: String): Result<List<JournalEntry>>
    suspend fun getOnThisDay(coupleId: String, monthDay: String, currentYear: String): Result<List<JournalEntry>>
    suspend fun upsertJournalEntry(entry: JournalEntry): Result<JournalEntry>
    suspend fun deleteJournalEntry(id: String): Result<Unit>
    fun journalPhotosFlow(entryId: String): Flow<List<JournalPhoto>>
    suspend fun addJournalPhoto(photo: JournalPhoto): Result<JournalPhoto>
    suspend fun deleteJournalPhoto(id: String): Result<Unit>

    // Daily Check-in
    suspend fun getCheckin(coupleId: String, userId: String, date: String): Result<DailyCheckin?>
    suspend fun getPartnerCheckin(coupleId: String, userId: String, date: String): Result<DailyCheckin?>
    suspend fun getWeeklyMoods(coupleId: String, startDate: String, endDate: String): Result<List<DailyCheckin>>
    suspend fun upsertCheckin(checkin: DailyCheckin): Result<DailyCheckin>
}
