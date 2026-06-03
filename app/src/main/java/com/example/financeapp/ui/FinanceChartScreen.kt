package com.example.financeapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.charts.*

@Composable
fun FinanceChartScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToComparison: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.chartState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Аналитика",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(
            onClick = onNavigateToComparison,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сравнение периодов →")
        }

        // Селектор периода
        PeriodSelector(onRangeSelected = viewModel::updatePeriod)

        // Переключатель доходы/расходы
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

        // Итоговая сумма
        val totalAmount = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.totalExpense else uiState.totalIncome
        val categories = if (uiState.selectedType == TransactionType.EXPENSE)
            uiState.expenseCategories else uiState.incomeCategories

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.selectedType == TransactionType.EXPENSE)
                        "Итого расходов" else "Итого доходов",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.2f ₽".format(totalAmount / 100.0),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Круговая диаграмма
        if (categories.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "По категориям",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PieChart(
                        data = categories.mapIndexed { index, it ->
                            PieChartData(
                                label = it.name,
                                value = it.total.toDouble(),
                                color = categoryColors[index % categoryColors.size]
                            )
                        },
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // Легенда
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Детализация",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChartLegend(categories)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Нет данных за период",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Столбчатая диаграмма (если есть данные)
        if (uiState.timeline.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Динамика",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ColumnChart(
                        models = uiState.timeline.map {
                            ColumnChartModel(it.date, it.amount)
                        },
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}