package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class MainComponent(
    componentContext: ComponentContext,
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
            Tab.Home -> TabChild.Home(componentContext)
            Tab.Wedding -> TabChild.Wedding(componentContext)
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
        val componentContext: ComponentContext

        data class Home(override val componentContext: ComponentContext) : TabChild
        data class Wedding(override val componentContext: ComponentContext) : TabChild
        data class Finance(override val componentContext: ComponentContext) : TabChild
        data class Us(override val componentContext: ComponentContext) : TabChild
        data class Me(override val componentContext: ComponentContext) : TabChild
    }
}
