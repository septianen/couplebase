package com.couplebase.feature.finance.savings

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.couplebase.core.model.SavingsContribution
import com.couplebase.core.model.SavingsGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(component: SavingsComponent) {
    val state by component.state.collectAsState()

    if (state.selectedGoal != null) {
        GoalDetailView(
            goal = state.selectedGoal!!,
            contributions = state.contributions,
            onBack = { component.onBackFromDetail() },
            onEdit = { component.onEditGoal(it) },
            onDelete = { component.onDeleteGoal(it) },
            onAddContribution = { component.onShowAddContribution(it) },
        )
    } else {
        GoalListView(
            state = state,
            onShowAddGoal = { component.onShowAddGoal() },
            onBack = component.onBack,
            onSelectGoal = { component.onSelectGoal(it) },
            onAddContribution = { component.onShowAddContribution(it) },
        )
    }

    if (state.showGoalSheet) {
        GoalBottomSheet(
            editing = state.editingGoal,
            onDismiss = { component.onDismissGoalSheet() },
            onSave = { title, target, date, icon ->
                component.onSaveGoal(title, target, date, icon)
            },
            onUpdate = { component.onUpdateGoal(it) },
            onDelete = { component.onDeleteGoal(it) },
        )
    }

    if (state.showContributionSheet) {
        ContributionBottomSheet(
            onDismiss = { component.onDismissContributionSheet() },
            onSave = { amount, note, date ->
                component.onSaveContribution(amount, note, date)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalListView(
    state: SavingsUiState,
    onShowAddGoal: () -> Unit,
    onBack: () -> Unit,
    onSelectGoal: (SavingsGoal) -> Unit,
    onAddContribution: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowAddGoal) {
                        Icon(Icons.Default.Add, contentDescription = "Add goal")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.goals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\uD83C\uDFAF",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No savings goals yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onShowAddGoal) {
                        Text("+ Add Goal")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.goals, key = { it.id }) { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onClick = { onSelectGoal(goal) },
                        onAddContribution = { onAddContribution(goal.id) },
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    onClick: () -> Unit,
    onAddContribution: () -> Unit,
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                goal.icon?.let {
                    Text(text = it, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Ring chart
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    RingChart(
                        progress = progress,
                        color = primaryColor,
                        trackColor = trackColor,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "$${formatAmount(goal.currentAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "of $${formatAmount(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    goal.targetDate?.let {
                        Text(
                            text = "Target: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onAddContribution,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ Add Contribution")
            }
        }
    }
}

@Composable
private fun RingChart(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2,
        )

        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Progress
        if (progress > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailView(
    goal: SavingsGoal,
    contributions: List<SavingsContribution>,
    onBack: () -> Unit,
    onEdit: (SavingsGoal) -> Unit,
    onDelete: (String) -> Unit,
    onAddContribution: (String) -> Unit,
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onEdit(goal) }) {
                        Text("Edit")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress ring
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            RingChart(
                                progress = progress,
                                color = primaryColor,
                                trackColor = trackColor.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxSize(),
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "$${formatAmount(goal.currentAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "of $${formatAmount(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        goal.targetDate?.let {
                            Text(
                                text = "Target: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }

            // Add contribution button
            item {
                OutlinedButton(
                    onClick = { onAddContribution(goal.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("+ Add Contribution")
                }
            }

            // Contributions
            item {
                Text(
                    text = "Contribution History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (contributions.isEmpty()) {
                item {
                    Text(
                        text = "No contributions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                items(contributions, key = { it.id }) { contribution ->
                    ContributionItem(contribution)
                }
            }

            // Delete button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { onDelete(goal.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Delete Goal",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ContributionItem(contribution: SavingsContribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = contribution.date,
                    style = MaterialTheme.typography.bodyMedium,
                )
                contribution.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "+$${formatAmount(contribution.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalBottomSheet(
    editing: SavingsGoal?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String?, String?) -> Unit,
    onUpdate: (SavingsGoal) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var targetAmount by remember { mutableStateOf(editing?.targetAmount?.let { formatAmount(it) } ?: "") }
    var targetDate by remember { mutableStateOf(editing?.targetDate ?: "") }
    var icon by remember { mutableStateOf(editing?.icon ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (editing != null) "Edit Goal" else "Add Savings Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal name") },
                placeholder = { Text("e.g. House Down Payment") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
            )

            OutlinedTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = { Text("Target date (YYYY-MM-DD, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = icon,
                onValueChange = { icon = it },
                label = { Text("Icon (emoji, optional)") },
                placeholder = { Text("e.g. \uD83C\uDFE0") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    val target = targetAmount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        if (editing != null) {
                            onUpdate(editing.copy(
                                title = title,
                                targetAmount = target,
                                targetDate = targetDate.ifBlank { null },
                                icon = icon.ifBlank { null },
                            ))
                        } else {
                            onSave(
                                title,
                                target,
                                targetDate.ifBlank { null },
                                icon.ifBlank { null },
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0,
            ) {
                Text(if (editing != null) "Update" else "Save")
            }

            if (editing != null) {
                TextButton(
                    onClick = {
                        onDelete(editing.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Delete Goal",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContributionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Double, String?, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Add Contribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt, note.ifBlank { null }, date)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
            ) {
                Text("Save")
            }
        }
    }
}

private fun todayString(): String {
    val date = com.couplebase.core.common.today()
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}

private fun formatAmount(amount: Double): String {
    val long = amount.toLong()
    if (amount == long.toDouble()) return long.toString()
    val cents = ((amount - long) * 100).toInt()
    return "$long.${cents.toString().padStart(2, '0')}"
}
