package com.couplebase.me

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.feature.couple.goals.GoalListScreen
import com.couplebase.feature.couple.profile.ProfileScreen
import com.couplebase.feature.settings.SettingsScreen

@Composable
fun MeTabScreen(component: MeTabComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is MeTabComponent.Child.Profile -> ProfileScreen(instance.component)
            is MeTabComponent.Child.Goals -> GoalListScreen(instance.component)
            is MeTabComponent.Child.Settings -> SettingsScreen(instance.component)
        }
    }
}
