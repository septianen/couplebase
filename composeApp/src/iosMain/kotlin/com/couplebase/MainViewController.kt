package com.couplebase

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.couplebase.di.StubAuthRepository
import com.couplebase.navigation.RootComponent
import com.couplebase.navigation.RootContent

fun MainViewController() = ComposeUIViewController {
    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        authRepository = StubAuthRepository(),
    )
    RootContent(rootComponent)
}
