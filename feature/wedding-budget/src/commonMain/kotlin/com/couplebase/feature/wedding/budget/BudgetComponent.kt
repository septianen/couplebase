package com.couplebase.feature.wedding.budget

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import com.couplebase.core.model.PaidBy
import com.couplebase.feature.wedding.budget.usecase.CategorySummary
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

class BudgetComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: BudgetRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        BudgetHandler(coupleId, repository)
    }

    val state: StateFlow<BudgetUiState> = handler.state

    fun onAddCategory(name: String, allocatedAmount: Double, icon: String?) {
        handler.addCategory(name, allocatedAmount, icon)
    }

    fun onDeleteCategory(id: String) {
        handler.deleteCategory(id)
    }

    fun onAddExpense(categoryId: String, description: String, amount: Double, paidBy: PaidBy, date: String) {
        handler.addExpense(categoryId, description, amount, paidBy, date)
    }

    fun onDeleteExpense(id: String) {
        handler.deleteExpense(id)
    }

    fun onShowAddCategorySheet() {
        handler.updateState { it.copy(showAddCategorySheet = true) }
    }

    fun onDismissAddCategorySheet() {
        handler.updateState { it.copy(showAddCategorySheet = false) }
    }

    fun onShowAddExpenseSheet(categoryId: String?) {
        handler.updateState { it.copy(showAddExpenseSheet = true, selectedCategoryId = categoryId) }
    }

    fun onDismissAddExpenseSheet() {
        handler.updateState { it.copy(showAddExpenseSheet = false, selectedCategoryId = null) }
    }

    fun onBackClicked() = onBack()
}

data class BudgetUiState(
    val categorySummaries: List<CategorySummary> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val totalAllocated: Double = 0.0,
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = true,
    val showAddCategorySheet: Boolean = false,
    val showAddExpenseSheet: Boolean = false,
    val selectedCategoryId: String? = null,
) {
    val remaining: Double get() = totalAllocated - totalSpent
    val isOverBudget: Boolean get() = totalSpent > totalAllocated
    val spentPercent: Float get() = if (totalAllocated > 0) (totalSpent / totalAllocated).toFloat().coerceIn(0f, 1.5f) else 0f
}

private class BudgetHandler(
    private val coupleId: String,
    private val repository: BudgetRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(BudgetUiState())
    val state: StateFlow<BudgetUiState> = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                repository.categoriesFlow(coupleId),
                repository.expensesFlow(coupleId),
            ) { categories, expenses ->
                val summaries = categories.map { cat ->
                    val spent = expenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
                    CategorySummary(category = cat, spent = spent)
                }.sortedBy { it.category.sortOrder }

                val totalAllocated = categories.sumOf { it.allocatedAmount }
                val totalSpent = expenses.sumOf { it.amount }
                val recent = expenses.sortedByDescending { it.date }.take(10)

                BudgetUiState(
                    categorySummaries = summaries,
                    recentExpenses = recent,
                    totalAllocated = totalAllocated,
                    totalSpent = totalSpent,
                    isLoading = false,
                )
            }.collect { newState ->
                _state.update { old ->
                    newState.copy(
                        showAddCategorySheet = old.showAddCategorySheet,
                        showAddExpenseSheet = old.showAddExpenseSheet,
                        selectedCategoryId = old.selectedCategoryId,
                    )
                }
            }
        }
    }

    fun updateState(transform: (BudgetUiState) -> BudgetUiState) {
        _state.update(transform)
    }

    fun addCategory(name: String, allocatedAmount: Double, icon: String?) {
        val category = BudgetCategory(
            id = generateUuid(),
            coupleId = coupleId,
            name = name,
            allocatedAmount = allocatedAmount,
            icon = icon,
            sortOrder = _state.value.categorySummaries.size,
            updatedAt = "",
        )
        scope.launch { repository.upsertCategory(category) }
        _state.update { it.copy(showAddCategorySheet = false) }
    }

    fun deleteCategory(id: String) {
        scope.launch { repository.deleteCategory(id) }
    }

    fun addExpense(categoryId: String, description: String, amount: Double, paidBy: PaidBy, date: String) {
        val expense = Expense(
            id = generateUuid(),
            coupleId = coupleId,
            categoryId = categoryId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            date = date,
            isWeddingExpense = true,
            updatedAt = "",
        )
        scope.launch { repository.upsertExpense(expense) }
        _state.update { it.copy(showAddExpenseSheet = false, selectedCategoryId = null) }
    }

    fun deleteExpense(id: String) {
        scope.launch { repository.deleteExpense(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
