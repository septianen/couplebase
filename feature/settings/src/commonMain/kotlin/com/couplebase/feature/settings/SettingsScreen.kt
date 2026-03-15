package com.couplebase.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.couplebase.core.model.ThemeMode
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbErrorBar
import com.couplebase.core.ui.component.CbTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            CbErrorBar(
                message = state.error,
                onDismiss = { component.onDismissError() },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            // Account section
            SectionHeader("Account")
            SettingsCard {
                SettingsRow(
                    icon = "\uD83D\uDC64",
                    title = state.user?.fullName ?: "User",
                    subtitle = state.user?.email,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = "\uD83D\uDD12",
                    title = "Change Password",
                    onClick = {},
                )
            }

            // Couple Space section
            SectionHeader("Couple Space")
            SettingsCard {
                SettingsRow(
                    icon = "\uD83D\uDD17",
                    title = "Invite Code",
                    subtitle = state.couple?.inviteCode,
                    trailing = {
                        Text(
                            text = if (state.inviteCodeCopied) "Copied!" else "Copy",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { component.onCopyInviteCode() }
                                .padding(8.dp),
                        )
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = "\uD83D\uDCC5",
                    title = "Wedding Date",
                    subtitle = state.couple?.weddingDate ?: "Not set",
                    onClick = { component.onShowWeddingDateEditor() },
                )
            }

            // Preferences section
            SectionHeader("Preferences")
            SettingsCard {
                SettingsRow(
                    icon = "\uD83C\uDFA8",
                    title = "Theme",
                    subtitle = state.preferences.themeMode.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    onClick = { component.onShowThemePicker() },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = "\uD83D\uDD14",
                    title = "Notifications",
                    checked = state.preferences.notificationsEnabled,
                    onCheckedChange = { component.onToggleNotifications(it) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = "\u2764\uFE0F",
                    title = "Daily Check-in Reminder",
                    checked = state.preferences.dailyCheckinReminder,
                    onCheckedChange = { component.onToggleCheckinReminder(it) },
                )
            }

            // Data section
            SectionHeader("Data")
            SettingsCard {
                SettingsRow(
                    icon = "\uD83D\uDCE4",
                    title = "Export Data",
                    subtitle = "Download your data as JSON",
                    onClick = {},
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = "\uD83D\uDD04",
                    title = "Sync Status",
                    subtitle = "All synced",
                    trailing = {
                        Text(
                            text = "\u2713",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
            }

            // Action buttons
            Spacer(modifier = Modifier.height(8.dp))

            CbButton(
                text = "Log Out",
                onClick = { component.onLogoutClicked() },
                style = CbButtonStyle.OUTLINED,
                modifier = Modifier.fillMaxWidth(),
            )

            CbButton(
                text = "Leave Couple Space",
                onClick = { component.onShowLeaveConfirm() },
                style = CbButtonStyle.OUTLINED,
                modifier = Modifier.fillMaxWidth(),
            )

            // App version
            Text(
                text = "couplebase v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Light,
            )

            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Theme picker bottom sheet
    if (state.showThemePicker) {
        ThemePickerSheet(
            currentTheme = state.preferences.themeMode,
            onSelect = { component.onThemeSelected(it) },
            onDismiss = { component.onDismissThemePicker() },
        )
    }

    // Wedding date editor bottom sheet
    if (state.showWeddingDateEditor) {
        WeddingDateSheet(
            currentDate = state.couple?.weddingDate ?: "",
            onSave = { component.onUpdateWeddingDate(it) },
            onDismiss = { component.onDismissWeddingDateEditor() },
        )
    }

    // Leave couple space confirmation
    if (state.showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { component.onDismissLeaveConfirm() },
            title = { Text("Leave Couple Space?") },
            text = { Text("This will remove you from the shared space. Your personal data will be kept.") },
            confirmButton = {
                TextButton(onClick = { component.onDismissLeaveConfirm() }) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { component.onDismissLeaveConfirm() }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: String,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, modifier = Modifier.padding(end = 12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerSheet(
    currentTheme: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Choose Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemeMode.entries.forEach { theme ->
                val icon = when (theme) {
                    ThemeMode.LIGHT -> "\u2600\uFE0F"
                    ThemeMode.DARK -> "\uD83C\uDF19"
                    ThemeMode.SYSTEM -> "\uD83D\uDCF1"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(theme) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = icon, modifier = Modifier.padding(end = 12.dp))
                    Text(
                        text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (theme == currentTheme) {
                        Text(
                            text = "\u2713",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeddingDateSheet(
    currentDate: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var date by remember { mutableStateOf(currentDate) }
    var dateError by remember { mutableStateOf<String?>(null) }
    val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

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
                text = "Wedding Date",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            CbTextField(
                value = date,
                onValueChange = { date = it; dateError = null },
                label = "Date (YYYY-MM-DD)",
                error = dateError,
                modifier = Modifier.fillMaxWidth(),
            )

            CbButton(
                text = "Save",
                onClick = {
                    when {
                        date.isBlank() -> dateError = "Date is required"
                        !datePattern.matches(date) -> dateError = "Use format YYYY-MM-DD"
                        else -> onSave(date)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
