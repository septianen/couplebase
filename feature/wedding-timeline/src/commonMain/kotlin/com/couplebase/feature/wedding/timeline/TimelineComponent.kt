package com.couplebase.feature.wedding.timeline

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.TimelineRepository
import com.couplebase.core.model.TimelineBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimelineComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: TimelineRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        TimelineHandler(coupleId, repository)
    }

    val state: StateFlow<TimelineUiState> = handler.state

    fun onShowAddSheet() {
        handler.updateState { it.copy(showSheet = true, editingBlock = null) }
    }

    fun onEditBlock(block: TimelineBlock) {
        handler.updateState { it.copy(showSheet = true, editingBlock = block) }
    }

    fun onDismissSheet() {
        handler.updateState { it.copy(showSheet = false, editingBlock = null) }
    }

    fun onAddBlock(
        title: String,
        startTime: String,
        durationMinutes: Int,
        location: String?,
        description: String?,
        assignedPeople: String?,
    ) {
        handler.addBlock(title, startTime, durationMinutes, location, description, assignedPeople)
    }

    fun onUpdateBlock(block: TimelineBlock) {
        handler.updateBlock(block)
    }

    fun onDeleteBlock(id: String) {
        handler.deleteBlock(id)
    }

    fun onBackClicked() = onBack()
}

data class TimelineUiState(
    val blocks: List<TimelineBlock> = emptyList(),
    val isLoading: Boolean = true,
    val showSheet: Boolean = false,
    val editingBlock: TimelineBlock? = null,
)

private class TimelineHandler(
    private val coupleId: String,
    private val repository: TimelineRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(TimelineUiState())
    val state: StateFlow<TimelineUiState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.timelineFlow(coupleId).collect { blocks ->
                _state.update { old ->
                    old.copy(
                        blocks = blocks,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateState(transform: (TimelineUiState) -> TimelineUiState) {
        _state.update(transform)
    }

    fun addBlock(
        title: String,
        startTime: String,
        durationMinutes: Int,
        location: String?,
        description: String?,
        assignedPeople: String?,
    ) {
        val block = TimelineBlock(
            id = generateUuid(),
            coupleId = coupleId,
            title = title,
            startTime = startTime,
            durationMinutes = durationMinutes,
            location = location,
            description = description,
            assignedPeople = assignedPeople,
            sortOrder = _state.value.blocks.size,
            updatedAt = "",
        )
        scope.launch { repository.upsert(block) }
        _state.update { it.copy(showSheet = false, editingBlock = null) }
    }

    fun updateBlock(block: TimelineBlock) {
        scope.launch { repository.upsert(block) }
        _state.update { it.copy(showSheet = false, editingBlock = null) }
    }

    fun deleteBlock(id: String) {
        scope.launch { repository.delete(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
