package com.couplebase.feature.finance.expenses

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.couplebase.core.model.Expense
import com.couplebase.core.model.PaidBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(component: ExpensesComponent) {
    val state by component.state.collectAsState()

    val filteredExpenses = remember(state.expenses, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.expenses
        else state.expenses.filter {
            it.description.contains(state.searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { component.onShowAddExpense() },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Search
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { component.onSearchChanged(it) },
                        placeholder = { Text("Search expenses...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (filteredExpenses.isEmpty()) {
                    item {
                        EmptyExpensesState(onAdd = { component.onShowAddExpense() })
                    }
                } else {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onClick = { component.onEditExpense(expense) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (state.showExpenseSheet) {
        AddExpenseSheet(
            editing = state.editingExpense,
            categories = state.categories,
            onDismiss = { component.onDismissExpenseSheet() },
            onSave = { amount, desc, catId, paidBy, date ->
                component.onSaveExpense(amount, desc, catId, paidBy, date)
            },
            onUpdate = { expense -> component.onUpdateExpense(expense) },
            onDelete = { id -> component.onDeleteExpense(id) },
        )
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row {
                    Text(
                        text = expense.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    expense.paidBy?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it.name.lowercase().replaceFirstChar { c -> c.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = "-$${formatAmount(expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun EmptyExpensesState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83D\uDCB3",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track your spending by adding expenses",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onAdd) {
            Text("+ Add Expense")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSheet(
    editing: Expense?,
    categories: List<com.couplebase.core.model.BudgetCategory>,
    onDismiss: () -> Unit,
    onSave: (Double, String, String?, PaidBy, String) -> Unit,
    onUpdate: (Expense) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf(editing?.amount?.let { formatAmount(it) } ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(editing?.categoryId) }
    var paidBy by remember { mutableStateOf(editing?.paidBy ?: PaidBy.ME) }
    var date by remember { mutableStateOf(editing?.date ?: today()) }

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
                text = if (editing != null) "Edit Expense" else "Add Expense",
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
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("e.g. Groceries, Electric bill") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Category chips
            if (categories.isNotEmpty()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                            },
                            label = { Text(cat.name, maxLines = 1) },
                        )
                    }
                }
            }

            // Paid by
            Text(
                text = "Paid by",
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PaidBy.entries.forEach { option ->
                    FilterChip(
                        selected = paidBy == option,
                        onClick = { paidBy = option },
                        label = {
                            Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                    )
                }
            }

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
                    if (amt > 0 && description.isNotBlank()) {
                        if (editing != null) {
                            onUpdate(editing.copy(
                                amount = amt,
                                description = description,
                                categoryId = selectedCategoryId,
                                paidBy = paidBy,
                                date = date,
                            ))
                        } else {
                            onSave(amt, description, selectedCategoryId, paidBy, date)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && description.isNotBlank(),
            ) {
                Text(if (editing != null) "Update" else "Save Expense")
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
                        text = "Delete Expense",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun today(): String {
    val date = com.couplebase.core.common.today()
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}

private fun formatAmount(amount: Double): String {
    val long = amount.toLong()
    if (amount == long.toDouble()) return long.toString()
    val cents = ((amount - long) * 100).toInt()
    return "$long.${cents.toString().padStart(2, '0')}"
}
