package com.couplebase.wedding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.feature.wedding.budget.BudgetScreen
import com.couplebase.feature.wedding.checklist.ChecklistScreen
import com.couplebase.feature.wedding.guests.GuestListScreen
import com.couplebase.feature.wedding.timeline.TimelineScreen
import com.couplebase.feature.wedding.vendors.VendorListScreen

@Composable
fun WeddingTabScreen(component: WeddingTabComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is WeddingTabComponent.Child.Hub -> WeddingHubScreen(component)
            is WeddingTabComponent.Child.Checklist -> ChecklistScreen(instance.component)
            is WeddingTabComponent.Child.Budget -> BudgetScreen(instance.component)
            is WeddingTabComponent.Child.Guests -> GuestListScreen(instance.component)
            is WeddingTabComponent.Child.Vendors -> VendorListScreen(instance.component)
            is WeddingTabComponent.Child.Timeline -> TimelineScreen(instance.component)
        }
    }
}
