package com.couplebase.us

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.feature.comm.checkin.CheckinScreen
import com.couplebase.feature.comm.journal.JournalScreen
import com.couplebase.feature.comm.notes.NotesListScreen

@Composable
fun UsTabScreen(component: UsTabComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is UsTabComponent.Child.Hub -> UsHubScreen(component)
            is UsTabComponent.Child.Checkin -> CheckinScreen(instance.component)
            is UsTabComponent.Child.Notes -> NotesListScreen(instance.component)
            is UsTabComponent.Child.Journal -> JournalScreen(instance.component)
        }
    }
}
