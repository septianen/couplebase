package com.couplebase.feature.finance.expenses

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.common.today
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.model.BudgetCategory
import com.couplebase.core.model.Expense
import com.couplebase.core.model.PaidBy
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

class ExpensesComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val budgetRepository: BudgetRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        ExpensesHandler(coupleId, budgetRepository)
    }

    val state: StateFlow<ExpensesUiState> = handler.state

    fun onShowAddExpense() {
        handler.updateState { it.copy(showExpenseSheet = true, editingExpense = null) }
    }

    fun onEditExpense(expense: Expense) {
        handler.updateState { it.copy(showExpenseSheet = true, editingExpense = expense) }
    }

    fun onDismissExpenseSheet() {
        handler.updateState { it.copy(showExpenseSheet = false, editingExpense = null) }
    }

    fun onSaveExpense(
        amount: Double,
        description: String,
        categoryId: String?,
        paidBy: PaidBy,
        date: String,
    ) {
        handler.saveExpense(amount, description, categoryId, paidBy, date)
    }

    fun onUpdateExpense(expense: Expense) {
        handler.updateExpense(expense)
    }

    fun onDeleteExpense(id: String) {
        handler.deleteExpense(id)
    }

    fun onSearchChanged(query: String) {
        handler.updateState { it.copy(searchQuery = query) }
    }
}

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<BudgetCategory> = emptyList(),
    val isLoading: Boolean = true,
    val showExpenseSheet: Boolean = false,
    val editingExpense: Expense? = null,
    val searchQuery: String = "",
)

private class ExpensesHandler(
    private val coupleId: String,
    private val budgetRepository: BudgetRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(ExpensesUiState())
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                budgetRepository.expensesFlow(coupleId),
                budgetRepository.categoriesFlow(coupleId),
            ) { expenses, categories ->
                expenses.filter { !it.isWeddingExpense } to categories
            }.collect { (expenses, categories) ->
                _state.update { old ->
                    old.copy(
                        expenses = expenses,
                        categories = categories,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateState(transform: (ExpensesUiState) -> ExpensesUiState) {
        _state.update(transform)
    }

    fun saveExpense(
        amount: Double,
        description: String,
        categoryId: String?,
        paidBy: PaidBy,
        date: String,
    ) {
        val expense = Expense(
            id = generateUuid(),
            coupleId = coupleId,
            categoryId = categoryId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            date = date,
            isWeddingExpense = false,
            updatedAt = "",
        )
        scope.launch { budgetRepository.upsertExpense(expense) }
        _state.update { it.copy(showExpenseSheet = false, editingExpense = null) }
    }

    fun updateExpense(expense: Expense) {
        scope.launch { budgetRepository.upsertExpense(expense) }
        _state.update { it.copy(showExpenseSheet = false, editingExpense = null) }
    }

    fun deleteExpense(id: String) {
        scope.launch { budgetRepository.deleteExpense(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
