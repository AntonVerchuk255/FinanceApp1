package com.example.financeapp.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Category
import com.example.financeapp.data.PeriodType
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.dashboard.formatAmount

@Composable
fun BudgetsScreen(viewModel: BudgetsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить бюджет")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Бюджеты", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Нет бюджетов",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Нажмите + чтобы добавить",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.budgets, key = { it.budget.id }) { item ->
                        BudgetCard(
                            item = item,
                            onDelete = { viewModel.deleteBudget(item.budget) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddBudgetSheet(
            categories = uiState.categories.filter { it.type == TransactionType.EXPENSE },
            onDismiss = { showAddSheet = false },
            onConfirm = { categoryId, limit, period ->
                viewModel.addBudget(categoryId, limit, period)
            }
        )
    }
}

@Composable
fun BudgetCard(item: BudgetWithSpent, onDelete: () -> Unit) {
    val progress = if (item.budget.limitAmount > 0)
        (item.spent.toFloat() / item.budget.limitAmount).coerceIn(0f, 1f) else 0f
    val isOverBudget = item.spent > item.budget.limitAmount
    val progressColor = when {
        isOverBudget -> Color(0xFFD50000)
        progress > 0.8f -> Color(0xFFFF6D00)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.categoryName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        text = if (item.budget.periodType == PeriodType.MONTHLY) "В месяц" else "В неделю",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Потрачено: ${formatAmount(item.spent)}",
                    fontSize = 13.sp,
                    color = if (isOverBudget) Color(0xFFD50000)
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Лимит: ${formatAmount(item.budget.limitAmount)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isOverBudget) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Превышение на ${formatAmount(item.spent - item.budget.limitAmount)}",
                    fontSize = 12.sp,
                    color = Color(0xFFD50000),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, PeriodType) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var limitText by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(PeriodType.MONTHLY) }
    var categoryExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Новый бюджет", style = MaterialTheme.typography.titleLarge)

            // Категория
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Лимит
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Лимит") },
                suffix = { Text("₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Период
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PeriodType.MONTHLY to "Месяц", PeriodType.WEEKLY to "Неделя")
                    .forEach { (period, label) ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(label) }
                        )
                    }
            }

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull()
                        if (selectedCategory != null && limit != null && limit > 0) {
                            onConfirm(selectedCategory!!.id, (limit * 100).toLong(), selectedPeriod)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Добавить")
                }
            }
        }
    }
}