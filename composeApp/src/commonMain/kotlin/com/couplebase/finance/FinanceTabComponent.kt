package com.couplebase.finance

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.feature.finance.budget.MonthlyBudgetComponent
import com.couplebase.feature.finance.expenses.ExpensesComponent
import com.couplebase.feature.finance.savings.SavingsComponent
import kotlinx.serialization.Serializable

class FinanceTabComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val financeRepository: FinanceRepository,
    private val budgetRepository: BudgetRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Hub,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Hub -> Child.Hub
            Config.Budget -> Child.Budget(
                MonthlyBudgetComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    financeRepository = financeRepository,
                    budgetRepository = budgetRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Expenses -> Child.Expenses(
                ExpensesComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    budgetRepository = budgetRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Savings -> Child.Savings(
                SavingsComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    financeRepository = financeRepository,
                    onBack = { navigation.pop() },
                )
            )
        }
    }

    fun onNavigateToBudget() {
        navigation.push(Config.Budget)
    }

    fun onNavigateToExpenses() {
        navigation.push(Config.Expenses)
    }

    fun onNavigateToSavings() {
        navigation.push(Config.Savings)
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Hub : Config

        @Serializable
        data object Budget : Config

        @Serializable
        data object Expenses : Config

        @Serializable
        data object Savings : Config
    }

    sealed interface Child {
        data object Hub : Child
        data class Budget(val component: MonthlyBudgetComponent) : Child
        data class Expenses(val component: ExpensesComponent) : Child
        data class Savings(val component: SavingsComponent) : Child
    }
}
