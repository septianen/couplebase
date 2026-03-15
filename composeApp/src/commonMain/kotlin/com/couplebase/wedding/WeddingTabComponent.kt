package com.couplebase.wedding

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.domain.repository.BudgetRepository
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.feature.wedding.budget.BudgetComponent
import com.couplebase.feature.wedding.checklist.ChecklistComponent
import com.couplebase.feature.wedding.checklist.usecase.LoadChecklistTemplateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class WeddingTabComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val checklistRepository: ChecklistRepository,
    private val budgetRepository: BudgetRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    init {
        instanceKeeper.getOrCreate {
            TemplateLoader(coupleId, checklistRepository)
        }
    }

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Hub,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            Config.Hub -> Child.Hub
            Config.Checklist -> Child.Checklist(
                ChecklistComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = checklistRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Budget -> Child.Budget(
                BudgetComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = budgetRepository,
                    onBack = { navigation.pop() },
                )
            )
        }
    }

    fun onNavigateToChecklist() {
        navigation.push(Config.Checklist)
    }

    fun onNavigateToBudget() {
        navigation.push(Config.Budget)
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Hub : Config

        @Serializable
        data object Checklist : Config

        @Serializable
        data object Budget : Config
    }

    sealed interface Child {
        data object Hub : Child
        data class Checklist(val component: ChecklistComponent) : Child
        data class Budget(val component: BudgetComponent) : Child
    }
}

private class TemplateLoader(
    coupleId: String,
    repository: ChecklistRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val loadTemplate = LoadChecklistTemplateUseCase(repository)
        scope.launch {
            loadTemplate(coupleId)
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
