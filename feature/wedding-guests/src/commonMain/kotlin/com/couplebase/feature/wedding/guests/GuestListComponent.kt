package com.couplebase.feature.wedding.guests

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.model.Guest
import com.couplebase.core.model.RsvpStatus
import com.couplebase.feature.wedding.guests.usecase.ExportGuestListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuestListComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: GuestRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        GuestHandler(coupleId, repository)
    }

    val state: StateFlow<GuestListUiState> = handler.state

    fun onSearchQueryChanged(query: String) {
        handler.setSearchQuery(query)
    }

    fun onFilterSelected(filter: GuestFilter) {
        handler.setFilter(filter)
    }

    fun onAddGuest(
        name: String,
        email: String?,
        phone: String?,
        rsvpStatus: RsvpStatus,
        mealPreference: String?,
        tableNumber: Int?,
        hasPlusOne: Boolean,
        notes: String?,
    ) {
        handler.addGuest(name, email, phone, rsvpStatus, mealPreference, tableNumber, hasPlusOne, notes)
    }

    fun onUpdateGuest(guest: Guest) {
        handler.updateGuest(guest)
    }

    fun onDeleteGuest(id: String) {
        handler.deleteGuest(id)
    }

    fun onShowAddSheet() {
        handler.updateState { it.copy(showAddSheet = true, editingGuest = null) }
    }

    fun onEditGuest(guest: Guest) {
        handler.updateState { it.copy(showAddSheet = true, editingGuest = guest) }
    }

    fun onDismissSheet() {
        handler.updateState { it.copy(showAddSheet = false, editingGuest = null) }
    }

    fun onExportCsv(): String = handler.exportCsv()

    fun onBackClicked() = onBack()
}

enum class GuestFilter {
    ALL, ACCEPTED, DECLINED, PENDING
}

data class GuestListUiState(
    val guests: List<Guest> = emptyList(),
    val allGuests: List<Guest> = emptyList(),
    val searchQuery: String = "",
    val filter: GuestFilter = GuestFilter.ALL,
    val totalCount: Int = 0,
    val acceptedCount: Int = 0,
    val declinedCount: Int = 0,
    val pendingCount: Int = 0,
    val isLoading: Boolean = true,
    val showAddSheet: Boolean = false,
    val editingGuest: Guest? = null,
)

private class GuestHandler(
    private val coupleId: String,
    private val repository: GuestRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val searchQuery = MutableStateFlow("")
    private val filterFlow = MutableStateFlow(GuestFilter.ALL)
    private val _state = MutableStateFlow(GuestListUiState())
    val state: StateFlow<GuestListUiState> = _state.asStateFlow()
    private val exportUseCase = ExportGuestListUseCase()

    init {
        scope.launch {
            combine(
                repository.guestsFlow(coupleId),
                searchQuery,
                filterFlow,
            ) { guests, query, filter ->
                val filtered = guests
                    .let { list ->
                        if (query.isBlank()) list
                        else list.filter { it.name.contains(query, ignoreCase = true) }
                    }
                    .let { list ->
                        when (filter) {
                            GuestFilter.ALL -> list
                            GuestFilter.ACCEPTED -> list.filter { it.rsvpStatus == RsvpStatus.ACCEPTED }
                            GuestFilter.DECLINED -> list.filter { it.rsvpStatus == RsvpStatus.DECLINED }
                            GuestFilter.PENDING -> list.filter { it.rsvpStatus == RsvpStatus.PENDING }
                        }
                    }

                GuestListUiState(
                    guests = filtered,
                    allGuests = guests,
                    searchQuery = query,
                    filter = filter,
                    totalCount = guests.size,
                    acceptedCount = guests.count { it.rsvpStatus == RsvpStatus.ACCEPTED },
                    declinedCount = guests.count { it.rsvpStatus == RsvpStatus.DECLINED },
                    pendingCount = guests.count { it.rsvpStatus == RsvpStatus.PENDING },
                    isLoading = false,
                )
            }.collect { newState ->
                _state.update { old ->
                    newState.copy(
                        showAddSheet = old.showAddSheet,
                        editingGuest = old.editingGuest,
                    )
                }
            }
        }
    }

    fun updateState(transform: (GuestListUiState) -> GuestListUiState) {
        _state.update(transform)
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: GuestFilter) {
        filterFlow.value = filter
    }

    fun addGuest(
        name: String,
        email: String?,
        phone: String?,
        rsvpStatus: RsvpStatus,
        mealPreference: String?,
        tableNumber: Int?,
        hasPlusOne: Boolean,
        notes: String?,
    ) {
        val guest = Guest(
            id = generateUuid(),
            coupleId = coupleId,
            name = name,
            email = email,
            phone = phone,
            rsvpStatus = rsvpStatus,
            mealPreference = mealPreference,
            tableNumber = tableNumber,
            hasPlusOne = hasPlusOne,
            notes = notes,
            updatedAt = "",
        )
        scope.launch { repository.upsert(guest) }
        _state.update { it.copy(showAddSheet = false, editingGuest = null) }
    }

    fun updateGuest(guest: Guest) {
        scope.launch { repository.upsert(guest) }
        _state.update { it.copy(showAddSheet = false, editingGuest = null) }
    }

    fun deleteGuest(id: String) {
        scope.launch { repository.delete(id) }
    }

    fun exportCsv(): String = exportUseCase(_state.value.allGuests)

    override fun onDestroy() {
        scope.cancel()
    }
}
