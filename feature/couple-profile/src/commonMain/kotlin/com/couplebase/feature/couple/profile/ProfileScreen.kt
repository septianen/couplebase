package com.couplebase.feature.couple.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.LifeGoal
import com.couplebase.core.model.Milestone
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbTextField

@Composable
fun ProfileScreen(component: ProfileComponent) {
    val state by component.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Settings button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "\u2699\uFE0F",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .clickable { component.onNavigateToSettings() }
                            .padding(8.dp),
                    )
                }
            }

            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Couple avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "\uD83D\uDC91",
                            fontSize = 36.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Our Story",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Together since the beginning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Milestones section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CbButton(
                        text = "+ Add",
                        onClick = { component.onShowAddMilestone() },
                        style = CbButtonStyle.OUTLINED,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }

            // Milestone timeline
            if (state.milestones.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Add your first milestone together!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            itemsIndexed(state.milestones) { index, milestone ->
                MilestoneTimelineItem(
                    milestone = milestone,
                    isFirst = index == 0,
                    isLast = index == state.milestones.lastIndex,
                    onClick = { component.onEditMilestone(milestone) },
                )
            }

            // Life Goals section header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Life Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CbButton(
                        text = "View All →",
                        onClick = { component.onNavigateToGoals() },
                        style = CbButtonStyle.OUTLINED,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }

            // Goals summary cards
            if (state.goals.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Set your first life goal as a couple!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(state.goals.take(3)) { goal ->
                GoalSummaryCard(goal = goal)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showMilestoneSheet) {
        MilestoneBottomSheet(
            editing = state.editingMilestone,
            onDismiss = { component.onDismissMilestoneSheet() },
            onSave = { title, date, description, icon ->
                val editing = state.editingMilestone
                if (editing != null) {
                    component.onUpdateMilestone(
                        editing.copy(
                            title = title,
                            date = date,
                            description = description,
                            icon = icon,
                        )
                    )
                } else {
                    component.onAddMilestone(title, date, description, icon)
                }
            },
            onDelete = state.editingMilestone?.let { m ->
                { component.onDeleteMilestone(m.id) }
            },
        )
    }
}

@Composable
private fun MilestoneTimelineItem(
    milestone: Milestone,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        // Timeline connector
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                )
            }
        }

        // Milestone content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!milestone.icon.isNullOrBlank()) {
                        Text(text = milestone.icon!!, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = milestone.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!milestone.description.isNullOrBlank()) {
                    Text(
                        text = milestone.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalSummaryCard(goal: LifeGoal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (!goal.description.isNullOrBlank()) {
                Text(
                    text = goal.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MilestoneBottomSheet(
    editing: Milestone?,
    onDismiss: () -> Unit,
    onSave: (title: String, date: String, description: String?, icon: String?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var date by remember { mutableStateOf(editing?.date ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var icon by remember { mutableStateOf(editing?.icon ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (editing != null) "Edit Milestone" else "Add Milestone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            CbTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                modifier = Modifier.fillMaxWidth(),
            )

            CbTextField(
                value = date,
                onValueChange = { date = it },
                label = "Date (YYYY-MM-DD)",
                modifier = Modifier.fillMaxWidth(),
            )

            CbTextField(
                value = icon,
                onValueChange = { icon = it },
                label = "Icon (emoji)",
                modifier = Modifier.fillMaxWidth(),
            )

            CbTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                modifier = Modifier.fillMaxWidth(),
            )

            CbButton(
                text = if (editing != null) "Update" else "Add Milestone",
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank()) {
                        onSave(
                            title,
                            date,
                            description.ifBlank { null },
                            icon.ifBlank { null },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                CbButton(
                    text = "Delete",
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    style = CbButtonStyle.OUTLINED,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
