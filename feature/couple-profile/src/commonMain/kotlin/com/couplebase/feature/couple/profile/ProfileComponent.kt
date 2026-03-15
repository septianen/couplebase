package com.couplebase.feature.couple.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.core.model.LifeGoal
import com.couplebase.core.model.Milestone
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

class ProfileComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val milestoneRepository: MilestoneRepository,
    private val lifeGoalRepository: LifeGoalRepository,
    val onNavigateToGoals: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        ProfileHandler(coupleId, milestoneRepository, lifeGoalRepository)
    }

    val state: StateFlow<ProfileUiState> = handler.state

    fun onShowAddMilestone() {
        handler.updateState { it.copy(showMilestoneSheet = true, editingMilestone = null) }
    }

    fun onEditMilestone(milestone: Milestone) {
        handler.updateState { it.copy(showMilestoneSheet = true, editingMilestone = milestone) }
    }

    fun onDismissMilestoneSheet() {
        handler.updateState { it.copy(showMilestoneSheet = false, editingMilestone = null) }
    }

    fun onAddMilestone(title: String, date: String, description: String?, icon: String?) {
        handler.addMilestone(title, date, description, icon)
    }

    fun onUpdateMilestone(milestone: Milestone) {
        handler.updateMilestone(milestone)
    }

    fun onDeleteMilestone(id: String) {
        handler.deleteMilestone(id)
    }
}

data class ProfileUiState(
    val milestones: List<Milestone> = emptyList(),
    val goals: List<LifeGoal> = emptyList(),
    val isLoading: Boolean = true,
    val showMilestoneSheet: Boolean = false,
    val editingMilestone: Milestone? = null,
)

private class ProfileHandler(
    private val coupleId: String,
    private val milestoneRepository: MilestoneRepository,
    private val lifeGoalRepository: LifeGoalRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                milestoneRepository.milestonesFlow(coupleId),
                lifeGoalRepository.goalsFlow(coupleId),
            ) { milestones, goals ->
                milestones to goals
            }.collect { (milestones, goals) ->
                _state.update { old ->
                    old.copy(
                        milestones = milestones,
                        goals = goals,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateState(transform: (ProfileUiState) -> ProfileUiState) {
        _state.update(transform)
    }

    fun addMilestone(title: String, date: String, description: String?, icon: String?) {
        val milestone = Milestone(
            id = generateUuid(),
            coupleId = coupleId,
            title = title,
            date = date,
            description = description,
            icon = icon,
            sortOrder = _state.value.milestones.size,
            updatedAt = "",
        )
        scope.launch { milestoneRepository.upsert(milestone) }
        _state.update { it.copy(showMilestoneSheet = false, editingMilestone = null) }
    }

    fun updateMilestone(milestone: Milestone) {
        scope.launch { milestoneRepository.upsert(milestone) }
        _state.update { it.copy(showMilestoneSheet = false, editingMilestone = null) }
    }

    fun deleteMilestone(id: String) {
        scope.launch { milestoneRepository.delete(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
