package com.couplebase.feature.couple.goals

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.couplebase.core.model.GoalMilestone
import com.couplebase.core.model.LifeGoal
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbTextField

@Composable
fun GoalListScreen(component: GoalListComponent) {
    val state by component.state.collectAsState()

    if (state.selectedGoal != null) {
        GoalDetailView(
            goal = state.selectedGoal!!,
            milestones = state.milestones,
            showMilestoneSheet = state.showMilestoneSheet,
            onBack = { component.onBackFromDetail() },
            onEdit = { component.onEditGoal(state.selectedGoal!!) },
            onDelete = { component.onDeleteGoal(state.selectedGoal!!.id) },
            onAddMilestone = { component.onShowAddMilestone() },
            onDismissMilestoneSheet = { component.onDismissMilestoneSheet() },
            onSaveMilestone = { title -> component.onAddMilestone(title) },
            onToggleMilestone = { id, completed -> component.onToggleMilestone(id, completed) },
            onDeleteMilestone = { id -> component.onDeleteMilestone(id) },
        )
    } else {
        GoalListView(
            state = state,
            onBack = { component.onBackClicked() },
            onAddGoal = { component.onShowAddGoal() },
            onSelectGoal = { component.onSelectGoal(it) },
            onDismissGoalSheet = { component.onDismissGoalSheet() },
            onSaveGoal = { title, desc, target ->
                val editing = state.editingGoal
                if (editing != null) {
                    component.onUpdateGoal(
                        editing.copy(title = title, description = desc, targetDate = target)
                    )
                } else {
                    component.onAddGoal(title, desc, target)
                }
            },
            onDeleteGoal = state.editingGoal?.let { g ->
                { component.onDeleteGoal(g.id) }
            },
        )
    }
}

@Composable
private fun GoalListView(
    state: GoalListUiState,
    onBack: () -> Unit,
    onAddGoal: () -> Unit,
    onSelectGoal: (LifeGoal) -> Unit,
    onDismissGoalSheet: () -> Unit,
    onSaveGoal: (title: String, description: String?, targetDate: String?) -> Unit,
    onDeleteGoal: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "Life Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    CbButton(
                        text = "+ Add",
                        onClick = onAddGoal,
                        style = CbButtonStyle.OUTLINED,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }

            if (state.goals.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "\uD83C\uDFAF",
                                style = MaterialTheme.typography.displayMedium,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No goals yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Set goals together as a couple!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(state.goals) { goal ->
                GoalCard(goal = goal, onClick = { onSelectGoal(goal) })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showGoalSheet) {
        GoalBottomSheet(
            editing = state.editingGoal,
            onDismiss = onDismissGoalSheet,
            onSave = onSaveGoal,
            onDelete = onDeleteGoal,
        )
    }
}

@Composable
private fun GoalCard(goal: LifeGoal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!goal.description.isNullOrBlank()) {
                Text(
                    text = goal.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (!goal.targetDate.isNullOrBlank()) {
                Text(
                    text = "Target: ${goal.targetDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun GoalDetailView(
    goal: LifeGoal,
    milestones: List<GoalMilestone>,
    showMilestoneSheet: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMilestone: () -> Unit,
    onDismissMilestoneSheet: () -> Unit,
    onSaveMilestone: (title: String) -> Unit,
    onToggleMilestone: (id: String, isCompleted: Boolean) -> Unit,
    onDeleteMilestone: (id: String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = onEdit) {
                        Text("✏️")
                    }
                }
            }

            // Goal info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!goal.description.isNullOrBlank()) {
                            Text(
                                text = goal.description!!,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LinearProgressIndicator(
                                progress = { goal.progress / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${goal.progress}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (!goal.targetDate.isNullOrBlank()) {
                            Text(
                                text = "Target: ${goal.targetDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            // Sub-milestones header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CbButton(
                        text = "+ Add Step",
                        onClick = onAddMilestone,
                        style = CbButtonStyle.OUTLINED,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }

            if (milestones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Break this goal into smaller steps!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(milestones) { milestone ->
                MilestoneCheckItem(
                    milestone = milestone,
                    onToggle = { onToggleMilestone(milestone.id, !milestone.isCompleted) },
                    onDelete = { onDeleteMilestone(milestone.id) },
                )
            }

            // Delete goal button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CbButton(
                    text = "Delete Goal",
                    onClick = onDelete,
                    style = CbButtonStyle.OUTLINED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showMilestoneSheet) {
        AddMilestoneSheet(
            onDismiss = onDismissMilestoneSheet,
            onSave = onSaveMilestone,
        )
    }
}

@Composable
private fun MilestoneCheckItem(
    milestone: GoalMilestone,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = milestone.isCompleted,
            onCheckedChange = { onToggle() },
        )
        Text(
            text = milestone.title,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            color = if (milestone.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Text("🗑️", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalBottomSheet(
    editing: LifeGoal?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String?, targetDate: String?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var targetDate by remember { mutableStateOf(editing?.targetDate ?: "") }

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
                text = if (editing != null) "Edit Goal" else "Add Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            CbTextField(
                value = title,
                onValueChange = { title = it },
                label = "Goal title",
                modifier = Modifier.fillMaxWidth(),
            )

            CbTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                modifier = Modifier.fillMaxWidth(),
            )

            CbTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = "Target date (YYYY-MM-DD)",
                modifier = Modifier.fillMaxWidth(),
            )

            CbButton(
                text = if (editing != null) "Update" else "Add Goal",
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            description.ifBlank { null },
                            targetDate.ifBlank { null },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMilestoneSheet(
    onDismiss: () -> Unit,
    onSave: (title: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }

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
                text = "Add Step",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            CbTextField(
                value = title,
                onValueChange = { title = it },
                label = "Step title",
                modifier = Modifier.fillMaxWidth(),
            )

            CbButton(
                text = "Add Step",
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
