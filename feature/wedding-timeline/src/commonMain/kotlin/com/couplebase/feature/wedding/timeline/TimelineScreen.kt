package com.couplebase.feature.wedding.timeline

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.TimelineBlock
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbTextField

@Composable
fun TimelineScreen(component: TimelineComponent) {
    val state by component.state.collectAsState()

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
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = component::onBackClicked) {
                        Text(text = "\u2190", fontSize = 20.sp)
                    }
                    Text(
                        text = "Wedding Day Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = component::onShowAddSheet) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Timeline blocks
            itemsIndexed(
                items = state.blocks,
                key = { _, block -> block.id },
            ) { index, block ->
                TimelineBlockRow(
                    block = block,
                    isLast = index == state.blocks.lastIndex,
                    onClick = { component.onEditBlock(block) },
                )
            }

            // Empty state
            if (state.blocks.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No events added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to build your wedding day schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showSheet) {
        AddEditBlockSheet(
            editingBlock = state.editingBlock,
            onDismiss = component::onDismissSheet,
            onSave = { title, startTime, duration, location, description, people ->
                val existing = state.editingBlock
                if (existing != null) {
                    component.onUpdateBlock(
                        existing.copy(
                            title = title,
                            startTime = startTime,
                            durationMinutes = duration,
                            location = location,
                            description = description,
                            assignedPeople = people,
                        )
                    )
                } else {
                    component.onAddBlock(title, startTime, duration, location, description, people)
                }
            },
            onDelete = state.editingBlock?.let { block ->
                { component.onDeleteBlock(block.id) }
            },
        )
    }
}

@Composable
private fun TimelineBlockRow(
    block: TimelineBlock,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp),
    ) {
        // Time label
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = block.startTime,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Timeline line + dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    block.location?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val durationText = formatDuration(block.durationMinutes)
                    if (block.location != null && durationText.isNotBlank()) {
                        Text(
                            text = " \u00B7 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (durationText.isNotBlank()) {
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                block.assignedPeople?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBlockSheet(
    editingBlock: TimelineBlock?,
    onDismiss: () -> Unit,
    onSave: (
        title: String, startTime: String, durationMinutes: Int,
        location: String?, description: String?, assignedPeople: String?,
    ) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(editingBlock?.title ?: "") }
    var startTime by remember { mutableStateOf(editingBlock?.startTime ?: "") }
    var duration by remember { mutableStateOf(editingBlock?.durationMinutes?.toString() ?: "") }
    var location by remember { mutableStateOf(editingBlock?.location ?: "") }
    var description by remember { mutableStateOf(editingBlock?.description ?: "") }
    var people by remember { mutableStateOf(editingBlock?.assignedPeople ?: "") }

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
                text = if (editingBlock != null) "Edit Event" else "New Event",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = title,
                onValueChange = { title = it },
                label = "Event title",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = "Start time (e.g. 10:00 AM)",
                    modifier = Modifier.weight(1f),
                )
                CbTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = "Duration (min)",
                    modifier = Modifier.weight(0.5f),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            CbTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            CbTextField(
                value = people,
                onValueChange = { people = it },
                label = "Assigned people",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            CbTextField(
                value = description,
                onValueChange = { description = it },
                label = "Notes",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = if (editingBlock != null) "Update Event" else "Add Event",
                onClick = {
                    if (title.isNotBlank() && startTime.isNotBlank()) {
                        onSave(
                            title,
                            startTime,
                            duration.toIntOrNull() ?: 0,
                            location.ifBlank { null },
                            description.ifBlank { null },
                            people.ifBlank { null },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CbButton(
                    text = "Delete Event",
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = CbButtonStyle.OUTLINED,
                )
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    if (minutes <= 0) return ""
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}
