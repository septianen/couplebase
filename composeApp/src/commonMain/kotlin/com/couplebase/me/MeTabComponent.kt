package com.couplebase.me

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.couplebase.core.datastore.PreferencesDataStore
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.domain.repository.CoupleRepository
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.feature.couple.goals.GoalListComponent
import com.couplebase.feature.couple.profile.ProfileComponent
import com.couplebase.feature.settings.SettingsComponent
import kotlinx.serialization.Serializable

class MeTabComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val milestoneRepository: MilestoneRepository,
    private val lifeGoalRepository: LifeGoalRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository,
    private val onLogout: () -> Unit,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Profile,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Profile -> Child.Profile(
                ProfileComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    milestoneRepository = milestoneRepository,
                    lifeGoalRepository = lifeGoalRepository,
                    onNavigateToGoals = { navigation.push(Config.Goals) },
                    onNavigateToSettings = { navigation.push(Config.Settings) },
                )
            )
            Config.Goals -> Child.Goals(
                GoalListComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = lifeGoalRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Settings -> Child.Settings(
                SettingsComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    preferencesDataStore = preferencesDataStore,
                    authRepository = authRepository,
                    coupleRepository = coupleRepository,
                    onBack = { navigation.pop() },
                    onLogout = onLogout,
                )
            )
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Profile : Config

        @Serializable
        data object Goals : Config

        @Serializable
        data object Settings : Config
    }

    sealed interface Child {
        data class Profile(val component: ProfileComponent) : Child
        data class Goals(val component: GoalListComponent) : Child
        data class Settings(val component: SettingsComponent) : Child
    }
}
