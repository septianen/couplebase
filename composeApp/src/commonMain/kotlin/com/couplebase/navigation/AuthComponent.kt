package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.feature.auth.login.LoginComponent
import com.couplebase.feature.auth.signup.SignupComponent
import kotlinx.serialization.Serializable

class AuthComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val onAuthComplete: () -> Unit,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Login,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Login -> Child.Login(
                LoginComponent(
                    componentContext = componentContext,
                    authRepository = authRepository,
                    onLoginSuccess = onAuthComplete,
                    onNavigateToSignup = { navigation.push(Config.Signup) },
                )
            )
            Config.Signup -> Child.Signup(
                SignupComponent(
                    componentContext = componentContext,
                    authRepository = authRepository,
                    onSignupSuccess = onAuthComplete,
                    onNavigateBack = { navigation.pop() },
                )
            )
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Signup : Config
    }

    sealed interface Child {
        data class Login(val component: LoginComponent) : Child
        data class Signup(val component: SignupComponent) : Child
    }
}
