package com.couplebase.feature.comm.journal

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.common.nowLocal
import com.couplebase.core.common.today
import com.couplebase.core.domain.repository.CommunicationRepository
import com.couplebase.core.model.JournalEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JournalComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val userId: String = "stub-user-id",
    private val repository: CommunicationRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        JournalHandler(coupleId, userId, repository)
    }

    val state: StateFlow<JournalUiState> = handler.state

    fun onShowAddEntry() {
        handler.updateState { it.copy(showEntrySheet = true, editingEntry = null) }
    }

    fun onEditEntry(entry: JournalEntry) {
        handler.updateState { it.copy(showEntrySheet = true, editingEntry = entry) }
    }

    fun onDismissSheet() {
        handler.updateState { it.copy(showEntrySheet = false, editingEntry = null) }
    }

    fun onSaveEntry(body: String, isShared: Boolean) {
        handler.saveEntry(body, isShared)
    }

    fun onDeleteEntry(id: String) {
        handler.deleteEntry(id)
    }
}

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val onThisDayEntries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val showEntrySheet: Boolean = false,
    val editingEntry: JournalEntry? = null,
)

private class JournalHandler(
    private val coupleId: String,
    private val userId: String,
    private val repository: CommunicationRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(JournalUiState())
    val state: StateFlow<JournalUiState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.journalFlow(coupleId).collect { entries ->
                _state.update { it.copy(entries = entries, isLoading = false) }
            }
        }
        loadOnThisDay()
    }

    private fun loadOnThisDay() {
        scope.launch {
            val dateStr = today().toString()
            val monthDay = dateStr.substring(5, 10)
            val currentYear = dateStr.substring(0, 4)
            when (val r = repository.getOnThisDay(coupleId, monthDay, currentYear)) {
                is Result.Success -> _state.update { it.copy(onThisDayEntries = r.data) }
                is Result.Error -> {}
            }
        }
    }

    fun updateState(transform: (JournalUiState) -> JournalUiState) {
        _state.update(transform)
    }

    fun saveEntry(body: String, isShared: Boolean) {
        val editing = _state.value.editingEntry
        val entry = JournalEntry(
            id = editing?.id ?: generateUuid(),
            coupleId = coupleId,
            authorId = userId,
            body = body,
            isShared = isShared,
            date = editing?.date ?: today().toString(),
            updatedAt = nowLocal().toString(),
            isDeleted = false,
        )
        scope.launch { repository.upsertJournalEntry(entry) }
        _state.update { it.copy(showEntrySheet = false, editingEntry = null) }
    }

    fun deleteEntry(id: String) {
        scope.launch { repository.deleteJournalEntry(id) }
        _state.update { it.copy(showEntrySheet = false, editingEntry = null) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
