package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.home.HomeTabComponent
import com.couplebase.wedding.WeddingTabComponent
import kotlinx.serialization.Serializable

class MainComponent(
    componentContext: ComponentContext,
    private val checklistRepository: ChecklistRepository,
    private val budgetRepository: BudgetRepository,
    private val guestRepository: GuestRepository,
    private val vendorRepository: VendorRepository,
    private val coupleId: String = "stub-couple-id",
    val onLogout: () -> Unit,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Tab>()

    val childStack: Value<ChildStack<Tab, TabChild>> = childStack(
        source = navigation,
        serializer = Tab.serializer(),
        initialConfiguration = Tab.Home,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(tab: Tab, componentContext: ComponentContext): TabChild {
        return when (tab) {
            Tab.Home -> TabChild.Home(HomeTabComponent(componentContext))
            Tab.Wedding -> TabChild.Wedding(
                WeddingTabComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    checklistRepository = checklistRepository,
                    budgetRepository = budgetRepository,
                    guestRepository = guestRepository,
                    vendorRepository = vendorRepository,
                )
            )
            Tab.Finance -> TabChild.Finance(componentContext)
            Tab.Us -> TabChild.Us(componentContext)
            Tab.Me -> TabChild.Me(componentContext)
        }
    }

    fun onTabSelected(tab: Tab) {
        navigation.bringToFront(tab)
    }

    @Serializable
    sealed interface Tab {
        @Serializable
        data object Home : Tab

        @Serializable
        data object Wedding : Tab

        @Serializable
        data object Finance : Tab

        @Serializable
        data object Us : Tab

        @Serializable
        data object Me : Tab
    }

    sealed interface TabChild {
        data class Home(val component: HomeTabComponent) : TabChild
        data class Wedding(val component: WeddingTabComponent) : TabChild
        data class Finance(val componentContext: ComponentContext) : TabChild
        data class Us(val componentContext: ComponentContext) : TabChild
        data class Me(val componentContext: ComponentContext) : TabChild
    }
}
