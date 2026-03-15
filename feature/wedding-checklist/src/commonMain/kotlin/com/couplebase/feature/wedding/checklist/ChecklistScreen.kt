package com.couplebase.feature.wedding.checklist

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.AssignedTo
import com.couplebase.core.model.ChecklistFilter
import com.couplebase.core.model.ChecklistItem
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(component: ChecklistComponent) {
    val state by component.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = component::onBackClicked) {
                    Text(text = "\u2190", fontSize = 20.sp)
                }
                Text(
                    text = "Checklist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = component::onShowAddSheet) {
                    Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChecklistFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { component.onFilterSelected(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    ChecklistFilter.ALL -> "All"
                                    ChecklistFilter.MINE -> "Mine"
                                    ChecklistFilter.PARTNER -> "Partner"
                                    ChecklistFilter.COMPLETED -> "Done"
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Checklist groups
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                state.groups.forEach { group ->
                    val isCollapsed = group.category in state.collapsedCategories

                    item(key = "header-${group.category}") {
                        CategoryHeader(
                            category = group.category,
                            itemCount = group.items.size,
                            completedCount = group.items.count { it.isCompleted },
                            isCollapsed = isCollapsed,
                            onClick = { component.onToggleCategory(group.category) },
                        )
                    }

                    if (!isCollapsed) {
                        items(
                            items = group.items,
                            key = { it.id },
                        ) { item ->
                            ChecklistItemRow(
                                item = item,
                                onToggle = { component.onToggleItem(item.id) },
                            )
                        }
                    }
                }

                if (state.groups.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "No tasks found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    // Add item bottom sheet
    if (state.showAddSheet) {
        AddChecklistItemSheet(
            onDismiss = component::onDismissAddSheet,
            onAdd = component::onAddItem,
        )
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    itemCount: Int,
    completedCount: Int,
    isCollapsed: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$completedCount / $itemCount completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (isCollapsed) "\u25B8" else "\u25BE",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (item.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (item.isCompleted) {
                Text(
                    text = "\u2713",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row {
                item.assignedTo?.let { assigned ->
                    Text(
                        text = when (assigned) {
                            AssignedTo.ME -> "You"
                            AssignedTo.PARTNER -> "Partner"
                            AssignedTo.BOTH -> "Both"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item.dueDate?.let { due ->
                    Text(
                        text = "  \u00B7  Due $due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChecklistItemSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, assignedTo: AssignedTo, dueDate: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("6-12 Months Before") }
    var selectedAssigned by remember { mutableStateOf(AssignedTo.ME) }

    val categories = listOf(
        "12+ Months Before",
        "6-12 Months Before",
        "3-6 Months Before",
        "1-3 Months Before",
        "Final Month",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "New Task",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task name",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                categories.take(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat.replace(" Before", ""),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                categories.drop(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat.replace(" Before", ""),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Assigned to",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssignedTo.entries.forEach { assigned ->
                    FilterChip(
                        selected = selectedAssigned == assigned,
                        onClick = { selectedAssigned = assigned },
                        label = {
                            Text(
                                text = when (assigned) {
                                    AssignedTo.ME -> "Me"
                                    AssignedTo.PARTNER -> "Partner"
                                    AssignedTo.BOTH -> "Both"
                                },
                            )
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = "Save Task",
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, selectedCategory, selectedAssigned, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
