package com.couplebase.feature.wedding.vendors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.Vendor
import com.couplebase.core.model.VendorPayment
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbTextField

@Composable
fun VendorListScreen(component: VendorListComponent) {
    val state by component.state.collectAsState()

    if (state.selectedVendor != null) {
        VendorDetailContent(
            vendor = state.selectedVendor!!,
            payments = state.payments,
            totalPaid = state.totalPaid,
            showPaymentSheet = state.showPaymentSheet,
            editingPayment = state.editingPayment,
            onBack = component::onBackFromDetail,
            onEdit = { component.onEditVendor(state.selectedVendor!!) },
            onDelete = { component.onDeleteVendor(state.selectedVendor!!.id) },
            onShowPaymentSheet = component::onShowPaymentSheet,
            onEditPayment = component::onEditPayment,
            onDismissPaymentSheet = component::onDismissPaymentSheet,
            onAddPayment = component::onAddPayment,
            onUpdatePayment = component::onUpdatePayment,
            onMarkPaid = component::onMarkPaymentPaid,
            onDeletePayment = component::onDeletePayment,
        )
    } else {
        VendorListContent(
            vendors = state.vendors,
            isLoading = state.isLoading,
            onBack = component::onBackClicked,
            onAdd = component::onShowAddSheet,
            onSelect = component::onSelectVendor,
        )
    }

    if (state.showVendorSheet) {
        AddEditVendorSheet(
            editingVendor = state.editingVendor,
            onDismiss = component::onDismissVendorSheet,
            onSave = { name, category, phone, email, website, totalCost, notes ->
                val existing = state.editingVendor
                if (existing != null) {
                    component.onUpdateVendor(
                        existing.copy(
                            name = name,
                            category = category,
                            phone = phone,
                            email = email,
                            website = website,
                            totalCost = totalCost,
                            notes = notes,
                        )
                    )
                } else {
                    component.onAddVendor(name, category, phone, email, website, totalCost, notes)
                }
            },
            onDelete = state.editingVendor?.let { vendor ->
                { component.onDeleteVendor(vendor.id) }
            },
        )
    }
}

@Composable
private fun VendorListContent(
    vendors: List<Vendor>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSelect: (Vendor) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Text(text = "\u2190", fontSize = 20.sp)
                    }
                    Text(
                        text = "Vendors",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onAdd) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(vendors, key = { it.id }) { vendor ->
                VendorCard(vendor = vendor, onClick = { onSelect(vendor) })
            }

            if (vendors.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No vendors added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add your first vendor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun VendorCard(
    vendor: Vendor,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vendor.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = vendor.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (vendor.totalCost > 0) {
                    Text(
                        text = "$${formatAmount(vendor.totalCost)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// --- Vendor Detail ---

@Composable
private fun VendorDetailContent(
    vendor: Vendor,
    payments: List<VendorPayment>,
    totalPaid: Double,
    showPaymentSheet: Boolean,
    editingPayment: VendorPayment?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowPaymentSheet: () -> Unit,
    onEditPayment: (VendorPayment) -> Unit,
    onDismissPaymentSheet: () -> Unit,
    onAddPayment: (String, Double, String) -> Unit,
    onUpdatePayment: (VendorPayment) -> Unit,
    onMarkPaid: (String) -> Unit,
    onDeletePayment: (String) -> Unit,
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
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Text(text = "\u2190", fontSize = 20.sp)
                    }
                    Text(
                        text = "Vendors",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onEdit) {
                        Text(text = "\u270F\uFE0F", fontSize = 18.sp)
                    }
                }
            }

            // Vendor info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = vendor.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = vendor.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Contact info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    vendor.phone?.let {
                        Text(
                            text = "\uD83D\uDCDE $it",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    vendor.email?.let {
                        Text(
                            text = "\uD83D\uDCE7 $it",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    vendor.website?.let {
                        Text(
                            text = "\uD83C\uDF10 $it",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (vendor.phone == null && vendor.email == null && vendor.website == null) {
                        Text(
                            text = "No contact info",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Payment progress
            if (vendor.totalCost > 0) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Payment Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "$${formatAmount(totalPaid)} / $${formatAmount(vendor.totalCost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = if (vendor.totalCost > 0) (totalPaid / vendor.totalCost).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Payment schedule header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Payment Schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onShowPaymentSheet) {
                        Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(payments, key = { it.id }) { payment ->
                PaymentRow(
                    payment = payment,
                    onMarkPaid = { onMarkPaid(payment.id) },
                    onClick = { onEditPayment(payment) },
                )
            }

            if (payments.isEmpty()) {
                item {
                    Text(
                        text = "No payments scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Notes
            vendor.notes?.let { notes ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Delete button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CbButton(
                    text = "Delete Vendor",
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = CbButtonStyle.OUTLINED,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showPaymentSheet) {
        AddEditPaymentSheet(
            editingPayment = editingPayment,
            onDismiss = onDismissPaymentSheet,
            onSave = { description, amount, dueDate ->
                val existing = editingPayment
                if (existing != null) {
                    onUpdatePayment(
                        existing.copy(
                            description = description,
                            amount = amount,
                            dueDate = dueDate,
                        )
                    )
                } else {
                    onAddPayment(description, amount, dueDate)
                }
            },
            onDelete = editingPayment?.let { p ->
                { onDeletePayment(p.id) }
            },
        )
    }
}

@Composable
private fun PaymentRow(
    payment: VendorPayment,
    onMarkPaid: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (payment.isPaid) "\u25CF" else "\u25CB",
            fontSize = 16.sp,
            color = if (payment.isPaid) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = "$${formatAmount(payment.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = payment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!payment.isPaid) {
                Text(
                    text = "Due: ${payment.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (payment.isPaid) {
            Text(
                text = "\u2713",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF66BB6A),
                fontWeight = FontWeight.Bold,
            )
        } else {
            CbButton(
                text = "Paid",
                onClick = onMarkPaid,
                style = CbButtonStyle.TEXT,
            )
        }
    }
}

// --- Bottom Sheets ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditVendorSheet(
    editingVendor: Vendor?,
    onDismiss: () -> Unit,
    onSave: (
        name: String, category: String, phone: String?,
        email: String?, website: String?, totalCost: Double, notes: String?,
    ) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(editingVendor?.name ?: "") }
    var category by remember { mutableStateOf(editingVendor?.category ?: "") }
    var phone by remember { mutableStateOf(editingVendor?.phone ?: "") }
    var email by remember { mutableStateOf(editingVendor?.email ?: "") }
    var website by remember { mutableStateOf(editingVendor?.website ?: "") }
    var totalCost by remember { mutableStateOf(editingVendor?.totalCost?.let { if (it > 0) formatAmount(it) else "" } ?: "") }
    var notes by remember { mutableStateOf(editingVendor?.notes ?: "") }

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
                text = if (editingVendor != null) "Edit Vendor" else "New Vendor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = name,
                onValueChange = { name = it },
                label = "Vendor name",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            CbTextField(
                value = category,
                onValueChange = { category = it },
                label = "Category (e.g. Photography, Florist)",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    modifier = Modifier.weight(1f),
                )
                CbTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = "Website",
                    modifier = Modifier.weight(1f),
                )
                CbTextField(
                    value = totalCost,
                    onValueChange = { totalCost = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Total cost",
                    modifier = Modifier.weight(0.6f),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
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
                text = if (editingVendor != null) "Update Vendor" else "Add Vendor",
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank()) {
                        onSave(
                            name,
                            category,
                            phone.ifBlank { null },
                            email.ifBlank { null },
                            website.ifBlank { null },
                            totalCost.toDoubleOrNull() ?: 0.0,
                            notes.ifBlank { null },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CbButton(
                    text = "Delete Vendor",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPaymentSheet(
    editingPayment: VendorPayment?,
    onDismiss: () -> Unit,
    onSave: (description: String, amount: Double, dueDate: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var description by remember { mutableStateOf(editingPayment?.description ?: "") }
    var amount by remember { mutableStateOf(editingPayment?.amount?.let { formatAmount(it) } ?: "") }
    var dueDate by remember { mutableStateOf(editingPayment?.dueDate ?: "") }

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
                text = if (editingPayment != null) "Edit Payment" else "New Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (e.g. Deposit, Final payment)",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CbTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Amount",
                    modifier = Modifier.weight(1f),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                )
                CbTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "Due date (YYYY-MM-DD)",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = if (editingPayment != null) "Update Payment" else "Add Payment",
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amt != null && amt > 0) {
                        onSave(description, amt, dueDate)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CbButton(
                    text = "Delete Payment",
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

private fun formatAmount(value: Double): String {
    val long = value.toLong()
    return if (value == long.toDouble()) "$long"
    else "${(value * 100).toLong() / 100.0}"
}
