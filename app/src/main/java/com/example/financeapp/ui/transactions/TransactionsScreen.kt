package com.example.financeapp.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.dashboard.formatAmount
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
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

            // Фильтры
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = uiState.filterType == TransactionType.INCOME,
                    onClick = { viewModel.setFilter(TransactionType.INCOME) },
                    label = { Text("Доходы") }
                )
                FilterChip(
                    selected = uiState.filterType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilter(TransactionType.EXPENSE) },
                    label = { Text("Расходы") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Нет операций",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            categoryName = uiState.categories
                                .find { it.id == transaction.categoryId }?.name,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = uiState.categories,
            onDismiss = { showAddSheet = false },
            onConfirm = { type, amount, categoryId, date, note ->
                viewModel.addTransaction(type, amount, categoryId, date, note)
            }
        )
    }
}

@Composable
fun TransactionListItem(
    transaction: Transaction,
    categoryName: String?,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        .withZone(ZoneId.systemDefault())

    val (color, sign) = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF00C853) to "+"
        TransactionType.EXPENSE -> Color(0xFFD50000) to "-"
        TransactionType.TRANSFER -> Color(0xFF2979FF) to ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName ?: when (transaction.type) {
                        TransactionType.INCOME -> "Доход"
                        TransactionType.EXPENSE -> "Расход"
                        TransactionType.TRANSFER -> "Перевод"
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (!transaction.note.isNullOrEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatter.format(transaction.date),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$sign${formatAmount(transaction.amount)}",
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}