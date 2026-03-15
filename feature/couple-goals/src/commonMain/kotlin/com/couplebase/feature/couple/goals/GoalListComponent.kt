package com.couplebase.feature.couple.goals

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.model.GoalMilestone
import com.couplebase.core.model.LifeGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalListComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: LifeGoalRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        GoalHandler(coupleId, repository)
    }

    val state: StateFlow<GoalListUiState> = handler.state

    fun onShowAddGoal() {
        handler.updateState { it.copy(showGoalSheet = true, editingGoal = null) }
    }

    fun onEditGoal(goal: LifeGoal) {
        handler.updateState { it.copy(showGoalSheet = true, editingGoal = goal) }
    }

    fun onDismissGoalSheet() {
        handler.updateState { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun onAddGoal(title: String, description: String?, targetDate: String?) {
        handler.addGoal(title, description, targetDate)
    }

    fun onUpdateGoal(goal: LifeGoal) {
        handler.updateGoal(goal)
    }

    fun onDeleteGoal(id: String) {
        handler.deleteGoal(id)
    }

    fun onSelectGoal(goal: LifeGoal) {
        handler.selectGoal(goal)
    }

    fun onBackFromDetail() {
        handler.updateState { it.copy(selectedGoal = null, milestones = emptyList()) }
    }

    fun onShowAddMilestone() {
        handler.updateState { it.copy(showMilestoneSheet = true, editingMilestone = null) }
    }

    fun onDismissMilestoneSheet() {
        handler.updateState { it.copy(showMilestoneSheet = false, editingMilestone = null) }
    }

    fun onAddMilestone(title: String) {
        handler.addMilestone(title)
    }

    fun onToggleMilestone(id: String, isCompleted: Boolean) {
        handler.toggleMilestone(id, isCompleted)
    }

    fun onDeleteMilestone(id: String) {
        handler.deleteMilestone(id)
    }

    fun onBackClicked() = onBack()
}

data class GoalListUiState(
    val goals: List<LifeGoal> = emptyList(),
    val isLoading: Boolean = true,
    val showGoalSheet: Boolean = false,
    val editingGoal: LifeGoal? = null,
    val selectedGoal: LifeGoal? = null,
    val milestones: List<GoalMilestone> = emptyList(),
    val showMilestoneSheet: Boolean = false,
    val editingMilestone: GoalMilestone? = null,
)

private class GoalHandler(
    private val coupleId: String,
    private val repository: LifeGoalRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(GoalListUiState())
    val state: StateFlow<GoalListUiState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.goalsFlow(coupleId).collect { goals ->
                _state.update { old ->
                    old.copy(
                        goals = goals,
                        isLoading = false,
                        selectedGoal = old.selectedGoal?.let { sel ->
                            goals.find { it.id == sel.id }
                        },
                    )
                }
            }
        }
    }

    fun updateState(transform: (GoalListUiState) -> GoalListUiState) {
        _state.update(transform)
    }

    fun selectGoal(goal: LifeGoal) {
        _state.update { it.copy(selectedGoal = goal, milestones = emptyList()) }
        scope.launch {
            repository.goalMilestonesFlow(goal.id).collect { milestones ->
                _state.update { old -> old.copy(milestones = milestones) }
            }
        }
    }

    fun addGoal(title: String, description: String?, targetDate: String?) {
        val goal = LifeGoal(
            id = generateUuid(),
            coupleId = coupleId,
            title = title,
            description = description,
            targetDate = targetDate,
            progress = 0,
            updatedAt = "",
        )
        scope.launch { repository.upsertGoal(goal) }
        _state.update { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun updateGoal(goal: LifeGoal) {
        scope.launch { repository.upsertGoal(goal) }
        _state.update { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun deleteGoal(id: String) {
        scope.launch { repository.deleteGoal(id) }
        _state.update { it.copy(selectedGoal = null, milestones = emptyList()) }
    }

    fun addMilestone(title: String) {
        val goalId = _state.value.selectedGoal?.id ?: return
        val milestone = GoalMilestone(
            id = generateUuid(),
            goalId = goalId,
            coupleId = coupleId,
            title = title,
            isCompleted = false,
            sortOrder = _state.value.milestones.size,
            updatedAt = "",
        )
        scope.launch { repository.upsertMilestone(milestone) }
        _state.update { it.copy(showMilestoneSheet = false, editingMilestone = null) }
    }

    fun toggleMilestone(id: String, isCompleted: Boolean) {
        scope.launch { repository.toggleMilestone(id, isCompleted) }
    }

    fun deleteMilestone(id: String) {
        scope.launch { repository.deleteMilestone(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
