package com.example.financeapp.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Goal
import com.example.financeapp.ui.dashboard.formatAmount
import java.time.Instant

val goalColors = listOf(
    "#6200EE", "#03DAC5", "#FF6D00", "#00C853",
    "#D50000", "#2979FF", "#FFD600", "#AA00FF"
)

@Composable
fun GoalsScreen(viewModel: GoalsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить цель")
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
            Text("Цели накопления", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.goals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Нет целей",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Нажмите + чтобы добавить цель",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onDelete = { viewModel.deleteGoal(goal) },
                            onDeposit = { selectedGoalForDeposit = goal }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddGoalSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, target, deadline, color ->
                viewModel.addGoal(name, target, deadline, color)
            }
        )
    }

    selectedGoalForDeposit?.let { goal ->
        DepositSheet(
            goal = goal,
            onDismiss = { selectedGoalForDeposit = null },
            onConfirm = { amount ->
                viewModel.addToGoal(goal.id, amount)
            }
        )
    }
}

@Composable
fun GoalCard(goal: Goal, onDelete: () -> Unit, onDeposit: () -> Unit) {
    val progress = if (goal.targetAmount > 0)
        (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
    val color = try { Color(android.graphics.Color.parseColor(goal.colorHex)) }
    catch (e: Exception) { Color(0xFF6200EE) }
    val isCompleted = goal.currentAmount >= goal.targetAmount

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(goal.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Row {
                    if (!isCompleted) {
                        TextButton(onClick = onDeposit) { Text("Пополнить") }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("✓ Цель достигнута!", fontSize = 13.sp,
                    color = Color(0xFF00C853), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatAmount(goal.currentAmount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatAmount(goal.targetAmount),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "%.1f%%".format(progress * 100),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Instant?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(goalColors[0]) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Новая цель", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название цели") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Целевая сумма") },
                suffix = { Text("₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Выбор цвета
            Text("Цвет", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                goalColors.forEach { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) }
                    catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == hex) 36.dp else 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull()
                        if (name.isNotBlank() && target != null && target > 0) {
                            onConfirm(name, (target * 100).toLong(), null, selectedColor)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Создать")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Пополнить «${goal.name}»",
                style = MaterialTheme.typography.titleLarge)
            Text(
                "Накоплено: ${formatAmount(goal.currentAmount)} из ${formatAmount(goal.targetAmount)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Сумма пополнения") },
                suffix = { Text("₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onConfirm((amount * 100).toLong())
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Пополнить")
                }
            }
        }
    }
}