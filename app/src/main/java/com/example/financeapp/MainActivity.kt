package com.example.financeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.ui.FinanceChartScreen
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionType
import com.example.financeapp.notifications.ReminderWorker
import com.example.financeapp.ui.navigation.AppNavHost
import com.example.financeapp.ui.navigation.BottomNavBar
import com.example.financeapp.ui.theme.FinanceAppTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) ReminderWorker.schedule(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрашиваем разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    ReminderWorker.schedule(this)
                }
                else -> requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        } else {
            ReminderWorker.schedule(this)
        }

        enableEdgeToEdge()
        setContent {
            FinanceAppTheme {
                val navController = rememberNavController()
                val db = AppDatabase.getInstance(applicationContext)

                LaunchedEffect(Unit) {
                    insertTestData(db)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        db = db,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

suspend fun insertTestData(db: AppDatabase) {
    val categoryDao = db.categoryDao()
    val transactionDao = db.transactionDao()

    val categories = listOf(
        // Расходы
        Category(id = 1, name = "Еда", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#6200EE"),
        Category(id = 2, name = "Транспорт", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#03DAC5"),
        Category(id = 3, name = "Развлечения", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#FF6D00"),
        Category(id = 4, name = "Здоровье", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#00C853"),
        Category(id = 5, name = "Одежда", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#D50000"),
        Category(id = 6, name = "Коммунальные", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#2979FF"),
        Category(id = 7, name = "Связь", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#FFD600"),
        Category(id = 8, name = "Прочее", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#AA00FF"),

        // Доходы
        Category(id = 9, name = "Зарплата", type = TransactionType.INCOME, iconRes = null, colorHex = "#00C853"),
        Category(id = 10, name = "Фриланс", type = TransactionType.INCOME, iconRes = null, colorHex = "#2979FF"),
        Category(id = 11, name = "Подарки", type = TransactionType.INCOME, iconRes = null, colorHex = "#FFD600"),
        Category(id = 12, name = "Инвестиции", type = TransactionType.INCOME, iconRes = null, colorHex = "#FF6D00"),
        Category(id = 13, name = "Прочее", type = TransactionType.INCOME, iconRes = null, colorHex = "#AA00FF"),
    )
    categories.forEach { categoryDao.insert(it) }

    // Тестовые транзакции
    //val now = Instant.now()
    //val transactions = listOf(
    //    Transaction(categoryId = 1, type = TransactionType.EXPENSE, amount = 150000, date = now.minus(1, ChronoUnit.DAYS), note = "Продукты"),
    //    Transaction(categoryId = 2, type = TransactionType.EXPENSE, amount = 50000, date = now.minus(2, ChronoUnit.DAYS), note = "Метро"),
    //    Transaction(categoryId = 3, type = TransactionType.EXPENSE, amount = 120000, date = now.minus(3, ChronoUnit.DAYS), note = "Кино"),
    //    Transaction(categoryId = 4, type = TransactionType.EXPENSE, amount = 80000, date = now.minus(5, ChronoUnit.DAYS), note = "Аптека"),
    //    Transaction(categoryId = 9, type = TransactionType.INCOME, amount = 15000000, date = now.minus(7, ChronoUnit.DAYS), note = "Зарплата за месяц"),
    //    Transaction(categoryId = 10, type = TransactionType.INCOME, amount = 500000, date = now.minus(10, ChronoUnit.DAYS), note = "Проект"),
    //)
    //transactions.forEach { transactionDao.insert(it) }
}