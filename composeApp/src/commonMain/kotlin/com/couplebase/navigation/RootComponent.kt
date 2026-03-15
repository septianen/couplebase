package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.domain.repository.CoupleRepository
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Auth,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Auth -> Child.Auth(
                AuthComponent(componentContext, authRepository, coupleRepository, ::onAuthComplete)
            )
            Config.Main -> Child.Main(MainComponent(componentContext))
        }
    }

    private fun onAuthComplete() {
        navigation.replaceAll(Config.Main)
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Auth : Config

        @Serializable
        data object Main : Config
    }

    sealed interface Child {
        data class Auth(val component: AuthComponent) : Child
        data class Main(val component: MainComponent) : Child
    }
}
