package com.couplebase.feature.finance.budget

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.couplebase.core.model.MonthlyBudget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyBudgetScreen(component: MonthlyBudgetComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budget") },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { component.onShowAddCategory() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add category")
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Month navigation
                item {
                    MonthNavigator(
                        displayMonth = state.displayMonth,
                        onPrevious = { component.onPreviousMonth() },
                        onNext = { component.onNextMonth() },
                    )
                }

                // Budget categories
                if (state.budgets.isEmpty()) {
                    item {
                        EmptyBudgetState(onAdd = { component.onShowAddCategory() })
                    }
                } else {
                    items(state.budgets, key = { it.id }) { budget ->
                        val spent = state.expenses
                            .filter { it.categoryId == budget.category || it.description == budget.category }
                            .sumOf { it.amount }
                        BudgetCategoryItem(
                            budget = budget,
                            spent = spent,
                            onClick = { component.onEditCategory(budget) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (state.showCategorySheet) {
        CategoryBottomSheet(
            editing = state.editingBudget,
            onDismiss = { component.onDismissCategorySheet() },
            onSave = { category, limit, income ->
                component.onSaveCategory(category, limit, income)
            },
            onUpdate = { budget -> component.onUpdateCategory(budget) },
            onDelete = { id -> component.onDeleteCategory(id) },
        )
    }
}

@Composable
private fun MonthNavigator(
    displayMonth: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = displayMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun BudgetCategoryItem(
    budget: MonthlyBudget,
    spent: Double,
    onClick: () -> Unit,
) {
    val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat().coerceIn(0f, 1.5f) else 0f
    val percentage = (progress * 100).toInt()
    val isOverBudget = percentage > 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${formatAmount(budget.limitAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isOverBudget) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "\u26A0\uFE0F",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = when {
                    isOverBudget -> MaterialTheme.colorScheme.error
                    percentage >= 90 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$${formatAmount(spent)} spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Normal,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyBudgetState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83D\uDCCA",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No budget categories yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add categories to track your monthly spending",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onAdd) {
            Text("+ Add Category")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBottomSheet(
    editing: MonthlyBudget?,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double, income: Double) -> Unit,
    onUpdate: (MonthlyBudget) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var category by remember { mutableStateOf(editing?.category ?: "") }
    var limitAmount by remember { mutableStateOf(editing?.limitAmount?.let { formatAmount(it) } ?: "") }
    var incomeAmount by remember { mutableStateOf(editing?.incomeAmount?.let { formatAmount(it) } ?: "") }

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
                text = if (editing != null) "Edit Category" else "Add Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category name") },
                placeholder = { Text("e.g. Housing, Food, Transport") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = limitAmount,
                onValueChange = { limitAmount = it },
                label = { Text("Budget limit") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
            )

            OutlinedTextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = { Text("Income amount (optional)") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    val limit = limitAmount.toDoubleOrNull() ?: 0.0
                    val income = incomeAmount.toDoubleOrNull() ?: 0.0
                    if (category.isNotBlank()) {
                        if (editing != null) {
                            onUpdate(editing.copy(
                                category = category,
                                limitAmount = limit,
                                incomeAmount = income,
                            ))
                        } else {
                            onSave(category, limit, income)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = category.isNotBlank(),
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
                        text = "Delete Category",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val long = amount.toLong()
    if (amount == long.toDouble()) return long.toString()
    val cents = ((amount - long) * 100).toInt()
    return "$long.${cents.toString().padStart(2, '0')}"
}
