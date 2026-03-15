package com.couplebase.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class HomeTabComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Dashboard,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Dashboard -> Child.Dashboard(
                HomeComponent(
                    componentContext = componentContext,
                    onNavigateToNotifications = { navigation.push(Config.Notifications) },
                )
            )
            Config.Notifications -> Child.Notifications(
                NotificationComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() },
                )
            )
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Dashboard : Config

        @Serializable
        data object Notifications : Config
    }

    sealed interface Child {
        data class Dashboard(val component: HomeComponent) : Child
        data class Notifications(val component: NotificationComponent) : Child
    }
}
