package com.couplebase.feature.wedding.budget

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplebase.core.model.PaidBy
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbTextField
import com.couplebase.feature.wedding.budget.usecase.CategorySummary

private val chartColors = listOf(
    Color(0xFFC2185B), // Rose
    Color(0xFF00897B), // Teal
    Color(0xFF5C6BC0), // Indigo
    Color(0xFFFFA726), // Orange
    Color(0xFF66BB6A), // Green
    Color(0xFFAB47BC), // Purple
    Color(0xFF42A5F5), // Blue
    Color(0xFFEF5350), // Red
    Color(0xFF26C6DA), // Cyan
    Color(0xFF8D6E63), // Brown
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(component: BudgetComponent) {
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
                        text = "Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = component::onShowAddCategorySheet) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Donut chart + totals
            item {
                BudgetOverviewCard(state)
            }

            // Category list
            if (state.categorySummaries.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${state.categorySummaries.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(
                    items = state.categorySummaries,
                    key = { it.category.id },
                ) { summary ->
                    CategoryRow(
                        summary = summary,
                        colorIndex = state.categorySummaries.indexOf(summary),
                        onAddExpense = { component.onShowAddExpenseSheet(summary.category.id) },
                    )
                }
            }

            // Recent expenses
            if (state.recentExpenses.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Expenses",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                items(
                    items = state.recentExpenses,
                    key = { it.id },
                ) { expense ->
                    val categoryName = state.categorySummaries
                        .find { it.category.id == expense.categoryId }
                        ?.category?.name ?: "Uncategorized"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "$categoryName \u00B7 ${expense.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = formatCurrency(expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Empty state
            if (state.categorySummaries.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No budget categories yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showAddCategorySheet) {
        AddCategorySheet(
            onDismiss = component::onDismissAddCategorySheet,
            onAdd = component::onAddCategory,
        )
    }

    if (state.showAddExpenseSheet) {
        AddExpenseSheet(
            categories = state.categorySummaries.map { it.category },
            selectedCategoryId = state.selectedCategoryId,
            onDismiss = component::onDismissAddExpenseSheet,
            onAdd = component::onAddExpense,
        )
    }
}

@Composable
private fun BudgetOverviewCard(state: BudgetUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Donut chart
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            DonutChart(
                summaries = state.categorySummaries,
                totalAllocated = state.totalAllocated,
                modifier = Modifier.fillMaxSize(),
            )
            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatCurrency(state.totalSpent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isOverBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "of ${formatCurrency(state.totalAllocated)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryItem("Budget", formatCurrency(state.totalAllocated), MaterialTheme.colorScheme.primary)
            SummaryItem("Spent", formatCurrency(state.totalSpent),
                if (state.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
            SummaryItem("Left", formatCurrency(state.remaining),
                if (state.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
        }

        if (state.isOverBudget) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(12.dp),
            ) {
                Text(
                    text = "\u26A0 Over budget by ${formatCurrency(-state.remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    summaries: List<CategorySummary>,
    totalAllocated: Double,
    modifier: Modifier = Modifier,
) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier.padding(12.dp)) {
        val strokeWidth = 28.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2,
        )
        val arcSize = Size(radius * 2, radius * 2)

        // Background arc
        drawArc(
            color = bgColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )

        if (totalAllocated > 0 && summaries.isNotEmpty()) {
            var currentAngle = -90f
            summaries.forEachIndexed { index, summary ->
                val sweep = (summary.spent / totalAllocated * 360).toFloat()
                if (sweep > 0.5f) {
                    drawArc(
                        color = chartColors[index % chartColors.size],
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                }
                currentAngle += sweep
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
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
private fun CategoryRow(
    summary: CategorySummary,
    colorIndex: Int,
    onAddExpense: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddExpense)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(chartColors[colorIndex % chartColors.size]),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = summary.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${formatCurrency(summary.spent)} / ${formatCurrency(summary.category.allocatedAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (summary.isOverBudget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "+ Expense",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { summary.spentPercent.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (summary.isOverBudget) MaterialTheme.colorScheme.error
            else chartColors[colorIndex % chartColors.size],
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, allocatedAmount: Double, icon: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val presets = listOf(
        "Venue", "Catering", "Photography", "Flowers",
        "Attire", "Music/DJ", "Decor", "Stationery",
        "Transportation", "Favors", "Other",
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
                text = "New Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = name,
                onValueChange = { name = it },
                label = "Category name",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quick presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                presets.take(4).forEach { preset ->
                    FilterChip(
                        selected = name == preset,
                        onClick = { name = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                presets.drop(4).take(4).forEach { preset ->
                    FilterChip(
                        selected = name == preset,
                        onClick = { name = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                presets.drop(8).forEach { preset ->
                    FilterChip(
                        selected = name == preset,
                        onClick = { name = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            CbTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Allocated budget",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            )
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = "Save Category",
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onAdd(name, amountValue, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSheet(
    categories: List<com.couplebase.core.model.BudgetCategory>,
    selectedCategoryId: String?,
    onDismiss: () -> Unit,
    onAdd: (categoryId: String, description: String, amount: Double, paidBy: PaidBy, date: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(selectedCategoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var paidBy by remember { mutableStateOf(PaidBy.ME) }

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
                text = "New Expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CbTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            CbTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Amount",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (categories.isNotEmpty()) {
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
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = cat.id },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                if (categories.size > 4) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        categories.drop(4).take(4).forEach { cat ->
                            FilterChip(
                                selected = categoryId == cat.id,
                                onClick = { categoryId = cat.id },
                                label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Paid by",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaidBy.entries.forEach { paid ->
                    FilterChip(
                        selected = paidBy == paid,
                        onClick = { paidBy = paid },
                        label = {
                            Text(
                                text = when (paid) {
                                    PaidBy.ME -> "Me"
                                    PaidBy.PARTNER -> "Partner"
                                    PaidBy.SPLIT -> "Split"
                                },
                            )
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            CbButton(
                text = "Save Expense",
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (description.isNotBlank() && amountValue > 0 && categoryId.isNotBlank()) {
                        onAdd(categoryId, description, amountValue, paidBy, "2026-03-15")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val abs = kotlin.math.abs(amount)
    val formatted = if (abs >= 1000) {
        val v = abs / 1000
        if (v == v.toLong().toDouble()) "${v.toLong()}k"
        else "${(v * 10).toLong() / 10.0}k"
    } else {
        if (abs == abs.toLong().toDouble()) "${abs.toLong()}"
        else "${abs.toLong()}"
    }
    return if (amount < 0) "-$$formatted" else "$$formatted"
}
