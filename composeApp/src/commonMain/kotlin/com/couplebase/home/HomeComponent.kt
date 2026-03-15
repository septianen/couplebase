package com.couplebase.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeComponent(
    componentContext: ComponentContext,
    private val onNavigateToChecklist: () -> Unit = {},
    private val onNavigateToBudget: () -> Unit = {},
    private val onNavigateToGuests: () -> Unit = {},
    private val onNavigateToNotifications: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { HomeHandler() }

    val state: StateFlow<HomeUiState> = handler.state

    fun onChecklistClicked() = onNavigateToChecklist()
    fun onBudgetClicked() = onNavigateToBudget()
    fun onGuestsClicked() = onNavigateToGuests()
    fun onNotificationsClicked() = onNavigateToNotifications()

    fun onCheckinMoodSelected(mood: String) {
        handler.selectMood(mood)
    }
}

data class HomeUiState(
    val coupleName: String = "Sarah & Mike",
    val weddingDate: String = "Jun 15, 2026",
    val daysToGo: Int = 92,
    val overallProgress: Float = 0.73f,
    val tasks: List<TaskPreview> = emptyList(),
    val budgetTotal: Double = 35_000.0,
    val budgetSpent: Double = 24_500.0,
    val selectedMood: String? = null,
)

data class TaskPreview(
    val id: String,
    val title: String,
    val dueDate: String,
    val assignedTo: String,
    val isCompleted: Boolean,
)

private class HomeHandler : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(
        HomeUiState(
            tasks = listOf(
                TaskPreview("1", "Book photographer", "Mar 15", "You", false),
                TaskPreview("2", "Finalize guest list", "Mar 20", "Both", false),
                TaskPreview("3", "Send save-the-dates", "Mar 10", "Partner", true),
            ),
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun selectMood(mood: String) {
        _state.value = _state.value.copy(selectedMood = mood)
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
