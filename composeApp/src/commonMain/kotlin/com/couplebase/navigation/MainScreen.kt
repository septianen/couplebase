package com.couplebase.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.couplebase.finance.FinanceTabScreen
import com.couplebase.home.HomeTabScreen
import com.couplebase.me.MeTabScreen
import com.couplebase.wedding.WeddingTabScreen

@Composable
fun MainScreen(component: MainComponent) {
    val childStack by component.childStack.subscribeAsState()
    val activeTab = childStack.active.configuration

    Scaffold(
        bottomBar = {
            NavigationBar {
                TabItem.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab.config,
                        onClick = { component.onTabSelected(tab.config) },
                        icon = {
                            Text(
                                text = tab.icon,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = Modifier.padding(innerPadding),
            animation = stackAnimation(fade()),
        ) { child ->
            when (val instance = child.instance) {
                is MainComponent.TabChild.Home -> HomeTabScreen(instance.component)
                is MainComponent.TabChild.Wedding -> WeddingTabScreen(instance.component)
                is MainComponent.TabChild.Finance -> FinanceTabScreen(instance.component)
                is MainComponent.TabChild.Us -> TabPlaceholder("Us")
                is MainComponent.TabChild.Me -> MeTabScreen(instance.component)
            }
        }
    }
}

@Composable
private fun TabPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class TabItem(
    val config: MainComponent.Tab,
    val icon: String,
    val label: String,
) {
    HOME(MainComponent.Tab.Home, "\uD83C\uDFE0", "Home"),
    WEDDING(MainComponent.Tab.Wedding, "\uD83D\uDC8D", "Wedding"),
    FINANCE(MainComponent.Tab.Finance, "\uD83D\uDCB0", "Finance"),
    US(MainComponent.Tab.Us, "\u2764\uFE0F", "Us"),
    ME(MainComponent.Tab.Me, "\uD83D\uDC64", "Me"),
}
