package com.example.financeapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.background
import androidx.glance.color.ColorProvider
import com.example.financeapp.MainActivity
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.dashboard.formatAmount
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height

class FinanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val dao = db.transactionDao()

        val now = Instant.now()
        val monthStart = now.minus(30, ChronoUnit.DAYS)

        val monthIncome = dao.getSumByTypeAndPeriodOnce(
            TransactionType.INCOME, monthStart, now) ?: 0L
        val monthExpense = dao.getSumByTypeAndPeriodOnce(
            TransactionType.EXPENSE, monthStart, now) ?: 0L
        val balance = (dao.getTotalIncomeOnce() ?: 0L) - (dao.getTotalExpenseOnce() ?: 0L)
        val recentTransactions = dao.getRecentTransactionsOnce(3)
        val categories = db.categoryDao().getAllCategoriesOnce()

        provideContent {
            WidgetContent(
                balance = balance,
                monthIncome = monthIncome,
                monthExpense = monthExpense,
                recentTransactions = recentTransactions.map { t ->
                    TransactionWidgetItem(
                        categoryName = categories.find { it.id == t.categoryId }?.name ?: "—",
                        amount = t.amount,
                        type = t.type
                    )
                }
            )
        }
    }
}

data class TransactionWidgetItem(
    val categoryName: String,
    val amount: Long,
    val type: TransactionType
)

@Composable
fun WidgetContent(
    balance: Long,
    monthIncome: Long,
    monthExpense: Long,
    recentTransactions: List<TransactionWidgetItem>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
            .clickable(onClick = actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        // Заголовок
        Text(
            text = "Финансы",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.glance.unit.ColorProvider(Color(0xFF6200EE))
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Баланс
        Text(
            text = "Баланс",
            style = TextStyle(
                fontSize = 11.sp,
                color = androidx.glance.unit.ColorProvider(Color.Gray)
            )
        )
        Text(
            text = formatAmount(balance),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.glance.unit.ColorProvider(Color.Black)
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Доходы и расходы за месяц
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Доходы",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = androidx.glance.unit.ColorProvider(Color.Gray)
                    )
                )
                Text(
                    text = formatAmount(monthIncome),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.glance.unit.ColorProvider(Color(0xFF00C853))
                    )
                )
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Расходы",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = androidx.glance.unit.ColorProvider(Color.Gray)
                    )
                )
                Text(
                    text = formatAmount(monthExpense),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.glance.unit.ColorProvider(Color(0xFFD50000))
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Последние транзакции
        if (recentTransactions.isNotEmpty()) {
            Text(
                text = "Последние операции",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = androidx.glance.unit.ColorProvider(Color.Gray)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            recentTransactions.forEach { item ->
                val color = when (item.type) {
                    TransactionType.INCOME -> Color(0xFF00C853)
                    TransactionType.EXPENSE -> Color(0xFFD50000)
                    TransactionType.TRANSFER -> Color(0xFF2979FF)
                }
                val sign = when (item.type) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.TRANSFER -> ""
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.Horizontal.End
                ) {
                    Text(
                        text = item.categoryName,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = androidx.glance.unit.ColorProvider(Color.Black)
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$sign${formatAmount(item.amount)}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.glance.unit.ColorProvider(color)
                        )
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Кнопка добавить
        Button(
            text = "+ Добавить",
            onClick = actionStartActivity<MainActivity>(),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

class FinanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FinanceWidget()
}