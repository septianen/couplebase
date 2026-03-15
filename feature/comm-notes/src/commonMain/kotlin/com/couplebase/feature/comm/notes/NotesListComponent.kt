package com.couplebase.feature.comm.notes

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.common.nowLocal
import com.couplebase.core.domain.repository.CommunicationRepository
import com.couplebase.core.model.SharedNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesListComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: CommunicationRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        NotesHandler(coupleId, repository)
    }

    val state: StateFlow<NotesUiState> = handler.state

    fun onShowAddNote() {
        handler.updateState { it.copy(showNoteSheet = true, editingNote = null) }
    }

    fun onEditNote(note: SharedNote) {
        handler.updateState { it.copy(showNoteSheet = true, editingNote = note) }
    }

    fun onDismissSheet() {
        handler.updateState { it.copy(showNoteSheet = false, editingNote = null) }
    }

    fun onSaveNote(title: String, body: String) {
        handler.saveNote(title, body)
    }

    fun onTogglePin(note: SharedNote) {
        handler.togglePin(note)
    }

    fun onDeleteNote(id: String) {
        handler.deleteNote(id)
    }
}

data class NotesUiState(
    val notes: List<SharedNote> = emptyList(),
    val isLoading: Boolean = true,
    val showNoteSheet: Boolean = false,
    val editingNote: SharedNote? = null,
)

private class NotesHandler(
    private val coupleId: String,
    private val repository: CommunicationRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.notesFlow(coupleId).collect { notes ->
                _state.update { it.copy(notes = notes, isLoading = false) }
            }
        }
    }

    fun updateState(transform: (NotesUiState) -> NotesUiState) {
        _state.update(transform)
    }

    fun saveNote(title: String, body: String) {
        val editing = _state.value.editingNote
        val note = SharedNote(
            id = editing?.id ?: generateUuid(),
            coupleId = coupleId,
            title = title,
            body = body,
            isPinned = editing?.isPinned ?: false,
            updatedAt = nowLocal().toString(),
            isDeleted = false,
        )
        scope.launch { repository.upsertNote(note) }
        _state.update { it.copy(showNoteSheet = false, editingNote = null) }
    }

    fun togglePin(note: SharedNote) {
        scope.launch { repository.toggleNotePin(note.id, !note.isPinned) }
    }

    fun deleteNote(id: String) {
        scope.launch { repository.deleteNote(id) }
        _state.update { it.copy(showNoteSheet = false, editingNote = null) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
