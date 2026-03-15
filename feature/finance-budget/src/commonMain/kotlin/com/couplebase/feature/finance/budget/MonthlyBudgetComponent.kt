package com.couplebase.feature.finance.budget

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.common.today
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.model.Expense
import com.couplebase.core.model.MonthlyBudget
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

class MonthlyBudgetComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val financeRepository: FinanceRepository,
    private val budgetRepository: BudgetRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        BudgetHandler(coupleId, financeRepository, budgetRepository)
    }

    val state: StateFlow<MonthlyBudgetUiState> = handler.state

    fun onPreviousMonth() {
        handler.navigateMonth(-1)
    }

    fun onNextMonth() {
        handler.navigateMonth(1)
    }

    fun onShowAddCategory() {
        handler.updateState { it.copy(showCategorySheet = true, editingBudget = null) }
    }

    fun onEditCategory(budget: MonthlyBudget) {
        handler.updateState { it.copy(showCategorySheet = true, editingBudget = budget) }
    }

    fun onDismissCategorySheet() {
        handler.updateState { it.copy(showCategorySheet = false, editingBudget = null) }
    }

    fun onSaveCategory(category: String, limitAmount: Double, incomeAmount: Double) {
        handler.saveCategory(category, limitAmount, incomeAmount)
    }

    fun onUpdateCategory(budget: MonthlyBudget) {
        handler.updateCategory(budget)
    }

    fun onDeleteCategory(id: String) {
        handler.deleteCategory(id)
    }
}

data class MonthlyBudgetUiState(
    val budgets: List<MonthlyBudget> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val yearMonth: String = "",
    val displayMonth: String = "",
    val totalIncome: Double = 0.0,
    val totalLimit: Double = 0.0,
    val isLoading: Boolean = true,
    val showCategorySheet: Boolean = false,
    val editingBudget: MonthlyBudget? = null,
)

private class BudgetHandler(
    private val coupleId: String,
    private val financeRepository: FinanceRepository,
    private val budgetRepository: BudgetRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(MonthlyBudgetUiState())
    val state: StateFlow<MonthlyBudgetUiState> = _state.asStateFlow()

    private var currentYear: Int
    private var currentMonth: Int

    init {
        val date = today()
        currentYear = date.year
        currentMonth = date.monthNumber
        loadMonth()
    }

    fun navigateMonth(delta: Int) {
        currentMonth += delta
        if (currentMonth > 12) {
            currentMonth = 1
            currentYear++
        } else if (currentMonth < 1) {
            currentMonth = 12
            currentYear--
        }
        loadMonth()
    }

    private fun loadMonth() {
        val yearMonth = "$currentYear-${currentMonth.toString().padStart(2, '0')}"
        val displayMonth = "${MONTH_NAMES[currentMonth - 1]} $currentYear"
        _state.update { it.copy(yearMonth = yearMonth, displayMonth = displayMonth, isLoading = true) }

        scope.launch {
            combine(
                financeRepository.monthlyBudgetsFlow(coupleId, yearMonth),
                budgetRepository.expensesFlow(coupleId),
            ) { budgets, allExpenses ->
                val monthExpenses = allExpenses.filter {
                    it.date.startsWith(yearMonth) && !it.isWeddingExpense
                }
                budgets to monthExpenses
            }.collect { (budgets, expenses) ->
                val totalIncome = budgets.sumOf { it.incomeAmount }
                val totalLimit = budgets.sumOf { it.limitAmount }
                _state.update { old ->
                    old.copy(
                        budgets = budgets,
                        expenses = expenses,
                        totalIncome = totalIncome,
                        totalLimit = totalLimit,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateState(transform: (MonthlyBudgetUiState) -> MonthlyBudgetUiState) {
        _state.update(transform)
    }

    fun saveCategory(category: String, limitAmount: Double, incomeAmount: Double) {
        val yearMonth = _state.value.yearMonth
        val budget = MonthlyBudget(
            id = generateUuid(),
            coupleId = coupleId,
            yearMonth = yearMonth,
            category = category,
            limitAmount = limitAmount,
            incomeAmount = incomeAmount,
            updatedAt = "",
        )
        scope.launch { financeRepository.upsertMonthlyBudget(budget) }
        _state.update { it.copy(showCategorySheet = false, editingBudget = null) }
    }

    fun updateCategory(budget: MonthlyBudget) {
        scope.launch { financeRepository.upsertMonthlyBudget(budget) }
        _state.update { it.copy(showCategorySheet = false, editingBudget = null) }
    }

    fun deleteCategory(id: String) {
        scope.launch { financeRepository.deleteMonthlyBudget(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }

    companion object {
        private val MONTH_NAMES = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )
    }
}
