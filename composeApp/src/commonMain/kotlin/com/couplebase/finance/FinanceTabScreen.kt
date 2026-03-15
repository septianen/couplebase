package com.couplebase.finance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.feature.finance.budget.MonthlyBudgetScreen

@Composable
fun FinanceTabScreen(component: FinanceTabComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is FinanceTabComponent.Child.Hub -> FinanceHubScreen(component)
            is FinanceTabComponent.Child.Budget -> MonthlyBudgetScreen(instance.component)
            is FinanceTabComponent.Child.Expenses -> PlaceholderScreen("Expenses")
            is FinanceTabComponent.Child.Savings -> PlaceholderScreen("Savings")
        }
    }
}
