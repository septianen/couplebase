package com.couplebase.us

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.couplebase.core.domain.repository.CommunicationRepository
import com.couplebase.feature.comm.checkin.CheckinComponent
import com.couplebase.feature.comm.journal.JournalComponent
import com.couplebase.feature.comm.notes.NotesListComponent
import kotlinx.serialization.Serializable

class UsTabComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val communicationRepository: CommunicationRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

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
            Config.Checkin -> Child.Checkin(
                CheckinComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = communicationRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Notes -> Child.Notes(
                NotesListComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = communicationRepository,
                    onBack = { navigation.pop() },
                )
            )
            Config.Journal -> Child.Journal(
                JournalComponent(
                    componentContext = componentContext,
                    coupleId = coupleId,
                    repository = communicationRepository,
                    onBack = { navigation.pop() },
                )
            )
        }
    }

    fun onNavigateToCheckin() {
        navigation.push(Config.Checkin)
    }

    fun onNavigateToNotes() {
        navigation.push(Config.Notes)
    }

    fun onNavigateToJournal() {
        navigation.push(Config.Journal)
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Hub : Config

        @Serializable
        data object Checkin : Config

        @Serializable
        data object Notes : Config

        @Serializable
        data object Journal : Config
    }

    sealed interface Child {
        data object Hub : Child
        data class Checkin(val component: CheckinComponent) : Child
        data class Notes(val component: NotesListComponent) : Child
        data class Journal(val component: JournalComponent) : Child
    }
}
