package com.couplebase.feature.auth.pairing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.CoupleRepository
import kotlinx.serialization.Serializable

class PairingComponent(
    componentContext: ComponentContext,
    private val coupleRepository: CoupleRepository,
    private val onPairingComplete: () -> Unit,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Choose,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Choose -> Child.Choose(
                ChooseComponent(
                    componentContext = componentContext,
                    onCreateSelected = { navigation.push(Config.Create) },
                    onJoinSelected = { navigation.push(Config.Join) },
                )
            )
            Config.Create -> Child.Create(
                CreateCoupleComponent(
                    componentContext = componentContext,
                    coupleRepository = coupleRepository,
                    onCoupleCreated = { inviteCode ->
                        navigation.push(Config.Invite(inviteCode))
                    },
                    onBack = { navigation.pop() },
                )
            )
            is Config.Invite -> Child.Invite(
                InviteComponent(
                    componentContext = componentContext,
                    inviteCode = config.inviteCode,
                    onContinue = onPairingComplete,
                )
            )
            Config.Join -> Child.Join(
                JoinCoupleComponent(
                    componentContext = componentContext,
                    coupleRepository = coupleRepository,
                    onJoinSuccess = onPairingComplete,
                    onBack = { navigation.pop() },
                )
            )
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Choose : Config

        @Serializable
        data object Create : Config

        @Serializable
        data class Invite(val inviteCode: String) : Config

        @Serializable
        data object Join : Config
    }

    sealed interface Child {
        data class Choose(val component: ChooseComponent) : Child
        data class Create(val component: CreateCoupleComponent) : Child
        data class Invite(val component: InviteComponent) : Child
        data class Join(val component: JoinCoupleComponent) : Child
    }
}
