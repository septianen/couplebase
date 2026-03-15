package com.couplebase.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { NotificationHandler() }

    val state: StateFlow<NotificationUiState> = handler.state

    fun onBackClicked() = onBack()
}

data class NotificationUiState(
    val groups: List<NotificationGroup> = emptyList(),
)

data class NotificationGroup(
    val label: String,
    val items: List<NotificationItem>,
)

data class NotificationItem(
    val id: String,
    val icon: String,
    val message: String,
    val timeAgo: String,
)

private class NotificationHandler : InstanceKeeper.Instance {

    val state: StateFlow<NotificationUiState> = MutableStateFlow(
        NotificationUiState(
            groups = listOf(
                NotificationGroup(
                    label = "Today",
                    items = listOf(
                        NotificationItem("1", "\uD83D\uDE0D", "Mike checked in", "2h ago"),
                        NotificationItem("2", "\u2713", "\"Save-the-dates\" completed", "4h ago"),
                    ),
                ),
                NotificationGroup(
                    label = "Yesterday",
                    items = listOf(
                        NotificationItem("3", "\uD83D\uDCB0", "Mike added expense \$89", "1d ago"),
                        NotificationItem("4", "\u23F0", "Payment reminder: DJ \$650", "1d ago"),
                    ),
                ),
                NotificationGroup(
                    label = "This Week",
                    items = listOf(
                        NotificationItem("5", "\uD83D\uDCDD", "\"Grocery List\" updated", "3d ago"),
                        NotificationItem("6", "\uD83D\uDC65", "3 guests RSVP'd", "4d ago"),
                    ),
                ),
            ),
        )
    ).asStateFlow()

    override fun onDestroy() {}
}
