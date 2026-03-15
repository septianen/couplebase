package com.couplebase.feature.wedding.checklist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.model.AssignedTo
import com.couplebase.core.model.ChecklistFilter
import com.couplebase.core.model.ChecklistItem
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

class ChecklistComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: ChecklistRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        ChecklistHandler(coupleId, repository)
    }

    val state: StateFlow<ChecklistUiState> = handler.state

    fun onFilterSelected(filter: ChecklistFilter) {
        handler.setFilter(filter)
    }

    fun onToggleItem(id: String) {
        handler.toggleItem(id)
    }

    fun onDeleteItem(id: String) {
        handler.deleteItem(id)
    }

    fun onToggleCategory(category: String) {
        handler.toggleCategory(category)
    }

    fun onAddItem(title: String, category: String, assignedTo: AssignedTo, dueDate: String?) {
        handler.addItem(title, category, assignedTo, dueDate)
    }

    fun onShowAddSheet() {
        handler.updateState { it.copy(showAddSheet = true) }
    }

    fun onDismissAddSheet() {
        handler.updateState { it.copy(showAddSheet = false) }
    }

    fun onBackClicked() = onBack()
}

data class ChecklistUiState(
    val filter: ChecklistFilter = ChecklistFilter.ALL,
    val groups: List<ChecklistGroup> = emptyList(),
    val collapsedCategories: Set<String> = emptySet(),
    val showAddSheet: Boolean = false,
    val isLoading: Boolean = true,
)

data class ChecklistGroup(
    val category: String,
    val items: List<ChecklistItem>,
)

private class ChecklistHandler(
    private val coupleId: String,
    private val repository: ChecklistRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val filterFlow = MutableStateFlow(ChecklistFilter.ALL)
    private val _state = MutableStateFlow(ChecklistUiState())
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                repository.checklistFlow(coupleId),
                filterFlow,
            ) { items, filter ->
                val filtered = applyFilter(items, filter)
                groupByCategory(filtered)
            }.collect { groups ->
                _state.update { it.copy(groups = groups, isLoading = false) }
            }
        }
    }

    fun updateState(transform: (ChecklistUiState) -> ChecklistUiState) {
        _state.update(transform)
    }

    fun setFilter(filter: ChecklistFilter) {
        filterFlow.value = filter
        _state.update { it.copy(filter = filter) }
    }

    fun toggleItem(id: String) {
        scope.launch {
            val result = repository.getById(id)
            if (result is com.couplebase.core.common.Result.Success) {
                val item = result.data ?: return@launch
                repository.toggleCompleted(id, !item.isCompleted)
            }
        }
    }

    fun deleteItem(id: String) {
        scope.launch {
            repository.delete(id)
        }
    }

    fun toggleCategory(category: String) {
        _state.update { state ->
            val collapsed = state.collapsedCategories.toMutableSet()
            if (category in collapsed) collapsed.remove(category) else collapsed.add(category)
            state.copy(collapsedCategories = collapsed)
        }
    }

    fun addItem(title: String, category: String, assignedTo: AssignedTo, dueDate: String?) {
        val newItem = ChecklistItem(
            id = generateUuid(),
            coupleId = coupleId,
            title = title,
            category = category,
            dueDate = dueDate,
            assignedTo = assignedTo,
            sortOrder = (_state.value.groups.sumOf { it.items.size }) + 1,
            updatedAt = "",
        )
        scope.launch {
            repository.upsert(newItem)
        }
        _state.update { it.copy(showAddSheet = false) }
    }

    private fun applyFilter(items: List<ChecklistItem>, filter: ChecklistFilter): List<ChecklistItem> =
        when (filter) {
            ChecklistFilter.ALL -> items
            ChecklistFilter.MINE -> items.filter { it.assignedTo == AssignedTo.ME }
            ChecklistFilter.PARTNER -> items.filter { it.assignedTo == AssignedTo.PARTNER }
            ChecklistFilter.COMPLETED -> items.filter { it.isCompleted }
        }

    private fun groupByCategory(items: List<ChecklistItem>): List<ChecklistGroup> =
        items
            .groupBy { it.category ?: "Other" }
            .map { (category, groupItems) ->
                ChecklistGroup(category, groupItems.sortedBy { it.sortOrder })
            }
            .sortedBy { categoryOrder(it.category) }

    private fun categoryOrder(category: String): Int = when (category) {
        "12+ Months Before" -> 0
        "6-12 Months Before" -> 1
        "3-6 Months Before" -> 2
        "1-3 Months Before" -> 3
        "Final Month" -> 4
        else -> 5
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
