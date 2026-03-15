package com.couplebase.feature.auth.pairing

import com.arkivanov.decompose.ComponentContext

class InviteComponent(
    componentContext: ComponentContext,
    val inviteCode: String,
    private val onContinue: () -> Unit,
) : ComponentContext by componentContext {

    fun onContinueClicked() = onContinue()
}
