package com.couplebase.feature.auth.pairing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun PairingScreen(component: PairingComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(slide() + fade()),
    ) { child ->
        when (val instance = child.instance) {
            is PairingComponent.Child.Choose -> ChooseScreen(instance.component)
            is PairingComponent.Child.Create -> CreateCoupleScreen(instance.component)
            is PairingComponent.Child.Invite -> InviteScreen(instance.component)
            is PairingComponent.Child.Join -> JoinCoupleScreen(instance.component)
        }
    }
}
