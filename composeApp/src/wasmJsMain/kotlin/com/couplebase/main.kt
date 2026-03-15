package com.couplebase

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.couplebase.navigation.RootComponent
import com.couplebase.navigation.RootContent

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponent(DefaultComponentContext(lifecycle = lifecycle))

    CanvasBasedWindow(canvasElementId = "ComposeTarget", title = "Couplebase") {
        RootContent(rootComponent)
    }
}
