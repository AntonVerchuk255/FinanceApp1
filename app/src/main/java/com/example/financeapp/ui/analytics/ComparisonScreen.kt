package com.example.financeapp.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.CategorySum
import com.example.financeapp.data.MonthlyTotal
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.charts.ColumnChart
import com.example.financeapp.ui.charts.ColumnChartModel
import com.example.financeapp.ui.dashboard.formatAmount
import java.time.Instant

@Composable
fun ComparisonScreen(viewModel: ComparisonViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Сравнение периодов", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Переключатель тип
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.selectedType == TransactionType.EXPENSE,
                onClick = { viewModel.setSelectedType(TransactionType.EXPENSE) },
                label = { Text("Расходы") }
            )
            FilterChip(
                selected = uiState.selectedType == TransactionType.INCOME,
                onClick = { viewModel.setSelectedType(TransactionType.INCOME) },
                label = { Text("Доходы") }
            )
        }

        // Карточки сравнения
        val currentTotal = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.currentPeriod.totalExpense else uiState.currentPeriod.totalIncome
        val prevTotal = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.previousPeriod.totalExpense else uiState.previousPeriod.totalIncome

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PeriodTotalCard(
                label = uiState.previousLabel,
                amount = prevTotal,
                modifier = Modifier.weight(1f)
            )
            PeriodTotalCard(
                label = uiState.currentLabel,
                amount = currentTotal,
                modifier = Modifier.weight(1f)
            )
        }

        // Карточка изменения
        DeltaCard(current = currentTotal, previous = prevTotal, type = uiState.selectedType)

        // График по месяцам
        val monthlyData = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.monthlyTotalsExpense else uiState.monthlyTotalsIncome

        if (monthlyData.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Динамика за 12 месяцев", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ColumnChart(
                        models = monthlyData.reversed().map {
                            ColumnChartModel(Instant.now(), it.total)
                        },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Подписи месяцев
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        monthlyData.reversed().takeLast(6).forEach { month ->
                            Text(
                                text = month.month.substring(5), // MM
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Сравнение по категориям
        val currentCats = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.currentPeriod.expenseCategories
        else uiState.currentPeriod.incomeCategories

        val prevCats = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.previousPeriod.expenseCategories
        else uiState.previousPeriod.incomeCategories

        if (currentCats.isNotEmpty() || prevCats.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("По категориям", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Заголовок таблицы
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Категория", modifier = Modifier.weight(2f),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(uiState.previousLabel.take(8),
                            modifier = Modifier.weight(1.5f),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End)
                        Text(uiState.currentLabel.take(8),
                            modifier = Modifier.weight(1.5f),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End)
                        Text("Δ", modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Все категории объединяем
                    val allNames = (currentCats.map { it.name } +
                            prevCats.map { it.name }).distinct()

                    allNames.forEach { name ->
                        val curr = currentCats.find { it.name == name }?.total ?: 0L
                        val prev = prevCats.find { it.name == name }?.total ?: 0L
                        CategoryComparisonRow(
                            name = name,
                            previous = prev,
                            current = curr,
                            type = uiState.selectedType
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodTotalCard(label: String, amount: Long, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAmount(amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DeltaCard(current: Long, previous: Long, type: TransactionType) {
    val delta = current - previous
    val percent = if (previous > 0) (delta.toFloat() / previous * 100) else 0f
    val isIncrease = delta > 0
    val isEqual = delta == 0L

    // Для расходов рост — плохо (красный), для доходов рост — хорошо (зелёный)
    val color = when {
        isEqual -> MaterialTheme.colorScheme.onSurfaceVariant
        type == TransactionType.EXPENSE -> if (isIncrease) Color(0xFFD50000) else Color(0xFF00C853)
        else -> if (isIncrease) Color(0xFF00C853) else Color(0xFFD50000)
    }

    val icon = when {
        isEqual -> Icons.Default.Remove
        isIncrease -> Icons.Default.ArrowUpward
        else -> Icons.Default.ArrowDownward
    }

    val label = when {
        isEqual -> "Без изменений"
        type == TransactionType.EXPENSE -> if (isIncrease) "Рост расходов" else "Снижение расходов"
        else -> if (isIncrease) "Рост доходов" else "Снижение доходов"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontWeight = FontWeight.SemiBold, color = color)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncrease) "+" else ""}${formatAmount(delta)}",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 16.sp
                )
                Text(
                    text = "${if (isIncrease) "+" else ""}${"%.1f".format(percent)}%",
                    fontSize = 13.sp,
                    color = color
                )
            }
        }
    }
}

@Composable
fun CategoryComparisonRow(name: String, previous: Long, current: Long, type: TransactionType) {
    val delta = current - previous
    val isIncrease = delta > 0
    val isEqual = delta == 0L
    // Для расходов рост — плохо (красный), для доходов рост — хорошо (зелёный)
    val color = when {
        isEqual -> MaterialTheme.colorScheme.onSurfaceVariant
        type == TransactionType.EXPENSE -> if (isIncrease) Color(0xFFD50000) else Color(0xFF00C853)
        else -> if (isIncrease) Color(0xFF00C853) else Color(0xFFD50000)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(2f), fontSize = 13.sp)
        Text(
            formatAmount(previous),
            modifier = Modifier.weight(1.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            formatAmount(current),
            modifier = Modifier.weight(1.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
        Text(
            text = when {
                delta == 0L -> "—"
                delta > 0 -> "+${formatAmount(delta)}"
                else -> formatAmount(delta)
            },
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}