package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.datastore.PreferencesDataStore
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.domain.repository.CoupleRepository
import com.couplebase.core.domain.repository.FinanceRepository
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.core.domain.repository.TimelineRepository
import com.couplebase.core.domain.repository.VendorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository,
    private val checklistRepository: ChecklistRepository,
    private val budgetRepository: BudgetRepository,
    private val guestRepository: GuestRepository,
    private val vendorRepository: VendorRepository,
    private val timelineRepository: TimelineRepository,
    private val milestoneRepository: MilestoneRepository,
    private val lifeGoalRepository: LifeGoalRepository,
    private val financeRepository: FinanceRepository,
    private val preferencesDataStore: PreferencesDataStore,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val handler = instanceKeeper.getOrCreate { SessionHandler(authRepository) }

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Splash,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    init {
        handler.checkSession { isLoggedIn ->
            if (isLoggedIn) {
                navigation.replaceAll(Config.Main)
            } else {
                navigation.replaceAll(Config.Auth)
            }
        }
    }

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Splash -> Child.Splash
            Config.Auth -> Child.Auth(
                AuthComponent(componentContext, authRepository, coupleRepository, ::onAuthComplete)
            )
            Config.Main -> Child.Main(
                MainComponent(
                    componentContext = componentContext,
                    checklistRepository = checklistRepository,
                    budgetRepository = budgetRepository,
                    guestRepository = guestRepository,
                    vendorRepository = vendorRepository,
                    timelineRepository = timelineRepository,
                    milestoneRepository = milestoneRepository,
                    lifeGoalRepository = lifeGoalRepository,
                    financeRepository = financeRepository,
                    preferencesDataStore = preferencesDataStore,
                    authRepository = authRepository,
                    coupleRepository = coupleRepository,
                    onLogout = ::onLogout,
                )
            )
        }
    }

    private fun onAuthComplete() {
        navigation.replaceAll(Config.Main)
    }

    private fun onLogout() {
        handler.logout {
            navigation.replaceAll(Config.Auth)
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Splash : Config

        @Serializable
        data object Auth : Config

        @Serializable
        data object Main : Config
    }

    sealed interface Child {
        data object Splash : Child
        data class Auth(val component: AuthComponent) : Child
        data class Main(val component: MainComponent) : Child
    }
}

private class SessionHandler(
    private val authRepository: AuthRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun checkSession(onResult: (Boolean) -> Unit) {
        scope.launch {
            val loggedIn = authRepository.isLoggedIn()
            onResult(loggedIn)
        }
    }

    fun logout(onComplete: () -> Unit) {
        scope.launch {
            authRepository.signOut()
            onComplete()
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
