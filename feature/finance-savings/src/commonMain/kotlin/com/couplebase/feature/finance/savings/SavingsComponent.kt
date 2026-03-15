package com.couplebase.feature.finance.savings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.SavingsContribution
import com.couplebase.core.model.SavingsGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavingsComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val financeRepository: FinanceRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        SavingsHandler(coupleId, financeRepository)
    }

    val state: StateFlow<SavingsUiState> = handler.state

    fun onShowAddGoal() {
        handler.updateState { it.copy(showGoalSheet = true, editingGoal = null) }
    }

    fun onDismissGoalSheet() {
        handler.updateState { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun onSaveGoal(title: String, targetAmount: Double, targetDate: String?, icon: String?) {
        handler.saveGoal(title, targetAmount, targetDate, icon)
    }

    fun onEditGoal(goal: SavingsGoal) {
        handler.updateState { it.copy(showGoalSheet = true, editingGoal = goal) }
    }

    fun onUpdateGoal(goal: SavingsGoal) {
        handler.updateGoal(goal)
    }

    fun onDeleteGoal(id: String) {
        handler.deleteGoal(id)
    }

    fun onShowAddContribution(goalId: String) {
        handler.updateState { it.copy(showContributionSheet = true, contributionGoalId = goalId) }
    }

    fun onDismissContributionSheet() {
        handler.updateState { it.copy(showContributionSheet = false, contributionGoalId = null) }
    }

    fun onSaveContribution(amount: Double, note: String?, date: String) {
        handler.saveContribution(amount, note, date)
    }

    fun onSelectGoal(goal: SavingsGoal) {
        handler.selectGoal(goal)
    }

    fun onBackFromDetail() {
        handler.updateState { it.copy(selectedGoal = null, contributions = emptyList()) }
    }
}

data class SavingsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = true,
    val showGoalSheet: Boolean = false,
    val editingGoal: SavingsGoal? = null,
    val selectedGoal: SavingsGoal? = null,
    val contributions: List<SavingsContribution> = emptyList(),
    val showContributionSheet: Boolean = false,
    val contributionGoalId: String? = null,
)

private class SavingsHandler(
    private val coupleId: String,
    private val financeRepository: FinanceRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SavingsUiState())
    val state: StateFlow<SavingsUiState> = _state.asStateFlow()

    init {
        scope.launch {
            financeRepository.savingsGoalsFlow(coupleId).collect { goals ->
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

    fun updateState(transform: (SavingsUiState) -> SavingsUiState) {
        _state.update(transform)
    }

    fun saveGoal(title: String, targetAmount: Double, targetDate: String?, icon: String?) {
        val goal = SavingsGoal(
            id = generateUuid(),
            coupleId = coupleId,
            title = title,
            targetAmount = targetAmount,
            targetDate = targetDate,
            icon = icon,
            updatedAt = "",
        )
        scope.launch { financeRepository.upsertSavingsGoal(goal) }
        _state.update { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun updateGoal(goal: SavingsGoal) {
        scope.launch { financeRepository.upsertSavingsGoal(goal) }
        _state.update { it.copy(showGoalSheet = false, editingGoal = null) }
    }

    fun deleteGoal(id: String) {
        scope.launch { financeRepository.deleteSavingsGoal(id) }
        _state.update { it.copy(selectedGoal = null) }
    }

    fun selectGoal(goal: SavingsGoal) {
        _state.update { it.copy(selectedGoal = goal) }
        scope.launch {
            financeRepository.contributionsFlow(goal.id).collect { contributions ->
                _state.update { old ->
                    if (old.selectedGoal?.id == goal.id) {
                        old.copy(contributions = contributions)
                    } else old
                }
            }
        }
    }

    fun saveContribution(amount: Double, note: String?, date: String) {
        val goalId = _state.value.contributionGoalId ?: return
        val contribution = SavingsContribution(
            id = generateUuid(),
            goalId = goalId,
            coupleId = coupleId,
            amount = amount,
            date = date,
            note = note,
            updatedAt = "",
        )
        scope.launch {
            financeRepository.addContribution(contribution)
            // Update goal's current amount
            val goal = _state.value.goals.find { it.id == goalId } ?: return@launch
            financeRepository.upsertSavingsGoal(
                goal.copy(currentAmount = goal.currentAmount + amount)
            )
        }
        _state.update { it.copy(showContributionSheet = false, contributionGoalId = null) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
