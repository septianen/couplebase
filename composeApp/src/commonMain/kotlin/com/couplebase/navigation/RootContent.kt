package com.couplebase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.core.ui.theme.CouplebaseTheme

@Composable
fun RootContent(component: RootComponent) {
    CouplebaseTheme {
        val childStack by component.childStack.subscribeAsState()

        Children(
            stack = childStack,
            animation = stackAnimation(fade()),
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Auth -> AuthScreen(instance.component)
                is RootComponent.Child.Main -> MainScreen(instance.component)
            }
        }
    }
}
