package com.couplebase.navigation

import com.arkivanov.decompose.ComponentContext

class AuthComponent(
    componentContext: ComponentContext,
    private val onAuthComplete: () -> Unit,
) : ComponentContext by componentContext {

    fun onLoginSuccess() {
        onAuthComplete()
    }
}
