package com.couplebase.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.ui.component.CbCard

@Composable
fun HomeScreen(component: HomeComponent) {
    val state by component.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "couplebase",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "\uD83D\uDD14",
                    fontSize = 24.sp,
                    modifier = Modifier.clickable { component.onNotificationsClicked() },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Wedding countdown card
            WeddingCountdownCard(state)
            Spacer(modifier = Modifier.height(16.dp))

            // Quick actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(
                onChecklistClicked = component::onChecklistClicked,
                onBudgetClicked = component::onBudgetClicked,
                onGuestsClicked = component::onGuestsClicked,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Today's tasks
            TodaysTasksSection(state.tasks)
            Spacer(modifier = Modifier.height(16.dp))

            // Daily check-in
            DailyCheckinSection(
                selectedMood = state.selectedMood,
                onMoodSelected = component::onCheckinMoodSelected,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Budget snapshot
            BudgetSnapshotCard(state)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeddingCountdownCard(state: HomeUiState) {
    CbCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = state.coupleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Wedding: ${state.weddingDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "\u2764\uFE0F",
                    fontSize = 24.sp,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${(state.overallProgress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.daysToGo} days to go",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onChecklistClicked: () -> Unit,
    onBudgetClicked: () -> Unit,
    onGuestsClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickActionItem("\uD83D\uDCCB", "Checklist", Modifier.weight(1f), onChecklistClicked)
        QuickActionItem("\uD83D\uDCB0", "Budget", Modifier.weight(1f), onBudgetClicked)
        QuickActionItem("\uD83D\uDC65", "Guests", Modifier.weight(1f), onGuestsClicked)
    }
}

@Composable
private fun QuickActionItem(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CbCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TodaysTasksSection(tasks: List<TaskPreview>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Today's Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    CbCard {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            tasks.forEachIndexed { index, task ->
                TaskRow(task)
                if (index < tasks.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskPreview) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (task.isCompleted) {
                Text(
                    text = "\u2713",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Due: ${task.dueDate}  \u00B7  ${task.assignedTo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailyCheckinSection(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
) {
    Text(
        text = "Daily Check-in",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    CbCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val moods = listOf("\uD83D\uDE0A", "\uD83D\uDE10", "\uD83D\uDE14", "\uD83D\uDE0D", "\uD83D\uDE24")
                moods.forEach { mood ->
                    Text(
                        text = mood,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (selectedMood == mood) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            )
                            .clickable { onMoodSelected(mood) }
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSnapshotCard(state: HomeUiState) {
    Text(
        text = "Budget Snapshot",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    CbCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$${formatAmount(state.budgetSpent)} / $${formatAmount(state.budgetTotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                val percent = ((state.budgetSpent / state.budgetTotal) * 100).toInt()
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.budgetSpent / state.budgetTotal).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val remaining = state.budgetTotal - state.budgetSpent
            Text(
                text = "$${formatAmount(remaining)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        val thousands = amount / 1000
        if (thousands == thousands.toLong().toDouble()) {
            "${thousands.toLong()}k"
        } else {
            val rounded = (thousands * 10).toLong() / 10.0
            "${rounded}k"
        }
    } else {
        amount.toInt().toString()
    }
}
