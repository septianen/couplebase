package com.couplebase.feature.auth.pairing

import com.arkivanov.decompose.ComponentContext

class ChooseComponent(
    componentContext: ComponentContext,
    private val onCreateSelected: () -> Unit,
    private val onJoinSelected: () -> Unit,
) : ComponentContext by componentContext {

    fun onCreateClicked() = onCreateSelected()
    fun onJoinClicked() = onJoinSelected()
}
