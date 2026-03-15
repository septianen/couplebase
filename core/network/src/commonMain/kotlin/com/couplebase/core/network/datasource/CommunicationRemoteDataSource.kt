package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.DailyCheckinDto
import com.couplebase.core.network.dto.JournalEntryDto
import com.couplebase.core.network.dto.JournalPhotoDto
import com.couplebase.core.network.dto.SharedNoteDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class CommunicationRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val checkinTable get() = client.postgrest["daily_checkins"]
    private val noteTable get() = client.postgrest["shared_notes"]
    private val journalTable get() = client.postgrest["journal_entries"]
    private val photoTable get() = client.postgrest["journal_photos"]

    // --- Daily Check-in ---

    suspend fun getCheckin(coupleId: String, userId: String, date: String): DailyCheckinDto? =
        checkinTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("user_id", userId) }
            filter { eq("date", date) }
        }.decodeList<DailyCheckinDto>().firstOrNull()

    suspend fun upsertCheckin(dto: DailyCheckinDto): DailyCheckinDto =
        checkinTable.upsert(dto) { select() }.decodeSingle()

    // --- Shared Notes ---

    suspend fun getNotes(coupleId: String): List<SharedNoteDto> =
        noteTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("is_pinned", Order.DESCENDING)
            order("updated_at", Order.DESCENDING)
        }.decodeList()

    suspend fun upsertNote(dto: SharedNoteDto): SharedNoteDto =
        noteTable.upsert(dto) { select() }.decodeSingle()

    suspend fun deleteNote(id: String) {
        noteTable.update({ set("is_deleted", true) }) { filter { eq("id", id) } }
    }

    // --- Journal ---

    suspend fun getEntries(coupleId: String): List<JournalEntryDto> =
        journalTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun upsertEntry(dto: JournalEntryDto): JournalEntryDto =
        journalTable.upsert(dto) { select() }.decodeSingle()

    suspend fun deleteEntry(id: String) {
        journalTable.update({ set("is_deleted", true) }) { filter { eq("id", id) } }
    }

    // --- Journal Photos ---

    suspend fun getPhotos(entryId: String): List<JournalPhotoDto> =
        photoTable.select {
            filter { eq("entry_id", entryId) }
            filter { eq("is_deleted", false) }
            order("sort_order", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertPhoto(dto: JournalPhotoDto): JournalPhotoDto =
        photoTable.upsert(dto) { select() }.decodeSingle()

    suspend fun deletePhoto(id: String) {
        photoTable.update({ set("is_deleted", true) }) { filter { eq("id", id) } }
    }
}
