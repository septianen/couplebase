package com.couplebase.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun HomeTabScreen(component: HomeTabComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is HomeTabComponent.Child.Dashboard -> HomeScreen(instance.component)
            is HomeTabComponent.Child.Notifications -> NotificationScreen(instance.component)
        }
    }
}
