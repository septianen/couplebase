package com.couplebase.feature.wedding.guests

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.Guest
import com.couplebase.core.model.RsvpStatus
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestListScreen(component: GuestListComponent) {
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
                        text = "Guests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = component::onShowAddSheet) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatBadge("Total", state.totalCount, MaterialTheme.colorScheme.primary)
                    StatBadge("Accepted", state.acceptedCount, Color(0xFF66BB6A))
                    StatBadge("Declined", state.declinedCount, Color(0xFFEF5350))
                    StatBadge("Pending", state.pendingCount, Color(0xFFFFA726))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Search bar
            item {
                CbTextField(
                    value = state.searchQuery,
                    onValueChange = component::onSearchQueryChanged,
                    label = "Search guests...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GuestFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick = { component.onFilterSelected(filter) },
                            label = {
                                Text(
                                    text = when (filter) {
                                        GuestFilter.ALL -> "All"
                                        GuestFilter.ACCEPTED -> "Accepted"
                                        GuestFilter.DECLINED -> "Declined"
                                        GuestFilter.PENDING -> "Pending"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Guest cards
            items(
                items = state.guests,
                key = { it.id },
            ) { guest ->
                GuestCard(
                    guest = guest,
                    onClick = { component.onEditGuest(guest) },
                )
            }

            // Empty state
            if (state.guests.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "No guests found"
                            else "No guests added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (state.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add your first guest",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showAddSheet) {
        AddEditGuestSheet(
            editingGuest = state.editingGuest,
            onDismiss = component::onDismissSheet,
            onSave = { name, email, phone, rsvpStatus, meal, table, plusOne, notes ->
                val existing = state.editingGuest
                if (existing != null) {
                    component.onUpdateGuest(
                        existing.copy(
                            name = name,
                            email = email,
                            phone = phone,
                            rsvpStatus = rsvpStatus,
                            mealPreference = meal,
                            tableNumber = table,
                            hasPlusOne = plusOne,
                            notes = notes,
                        )
                    )
                } else {
                    component.onAddGuest(name, email, phone, rsvpStatus, meal, table, plusOne, notes)
                }
            },
            onDelete = state.editingGuest?.let { guest ->
                { component.onDeleteGuest(guest.id) }
            },
        )
    }
}

@Composable
private fun StatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuestCard(
    guest: Guest,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = guest.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = guest.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (guest.hasPlusOne) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }
            Row {
                guest.tableNumber?.let { table ->
                    Text(
                        text = "Table $table",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                guest.mealPreference?.let { meal ->
                    val prefix = if (guest.tableNumber != null) " \u00B7 " else ""
                    Text(
                        text = "$prefix$meal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // RSVP badge
        val (badgeColor, badgeText) = when (guest.rsvpStatus) {
            RsvpStatus.ACCEPTED -> Color(0xFF66BB6A) to "Accepted"
            RsvpStatus.DECLINED -> Color(0xFFEF5350) to "Declined"
            RsvpStatus.PENDING -> Color(0xFFFFA726) to "Pending"
        }
        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGuestSheet(
    editingGuest: Guest?,
    onDismiss: () -> Unit,
    onSave: (
        name: String, email: String?, phone: String?,
        rsvpStatus: RsvpStatus, mealPreference: String?,
        tableNumber: Int?, hasPlusOne: Boolean, notes: String?,
    ) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(editingGuest?.name ?: "") }
    var email by remember { mutableStateOf(editingGuest?.email ?: "") }
    var phone by remember { mutableStateOf(editingGuest?.phone ?: "") }
    var rsvpStatus by remember { mutableStateOf(editingGuest?.rsvpStatus ?: RsvpStatus.PENDING) }
    var meal by remember { mutableStateOf(editingGuest?.mealPreference ?: "") }
    var table by remember { mutableStateOf(editingGuest?.tableNumber?.toString() ?: "") }
    var plusOne by remember { mutableStateOf(editingGuest?.hasPlusOne ?: false) }
    var notes by remember { mutableStateOf(editingGuest?.notes ?: "") }

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
                text = if (editingGuest != null) "Edit Guest" else "New Guest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    modifier = Modifier.weight(1f),
                )
                CbTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // RSVP status
            Text(
                text = "RSVP Status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RsvpStatus.entries.forEach { status ->
                    FilterChip(
                        selected = rsvpStatus == status,
                        onClick = { rsvpStatus = status },
                        label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = meal,
                    onValueChange = { meal = it },
                    label = "Meal preference",
                    modifier = Modifier.weight(1f),
                )
                CbTextField(
                    value = table,
                    onValueChange = { table = it.filter { c -> c.isDigit() } },
                    label = "Table #",
                    modifier = Modifier.weight(0.5f),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Plus one",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = plusOne,
                    onCheckedChange = { plusOne = it },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            CbTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = if (editingGuest != null) "Update Guest" else "Add Guest",
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            email.ifBlank { null },
                            phone.ifBlank { null },
                            rsvpStatus,
                            meal.ifBlank { null },
                            table.toIntOrNull(),
                            plusOne,
                            notes.ifBlank { null },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CbButton(
                    text = "Delete Guest",
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = com.couplebase.core.ui.component.CbButtonStyle.OUTLINED,
                )
            }
        }
    }
}
