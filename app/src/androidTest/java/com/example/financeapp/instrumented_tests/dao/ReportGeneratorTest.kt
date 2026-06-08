package com.example.financeapp.instrumented_tests.dao

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Category
import com.example.financeapp.data.ReportGenerator
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class ReportGeneratorTest {

    private lateinit var db: AppDatabase
    private lateinit var reportGenerator: ReportGenerator
    private lateinit var testFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        reportGenerator = ReportGenerator(db)
        testFile = File(context.cacheDir, "test_report.pdf")

        // Очищаем перед тестом
        if (testFile.exists()) {
            testFile.delete()
        }

        // Очищаем БД
        runBlocking {
            db.categoryDao().deleteAll()
            db.transactionDao().deleteAll()
            db.budgetDao().deleteAll()
            db.goalDao().deleteAll()
        }
    }

    @After
    fun cleanup() {
        testFile.delete()
        db.close()
    }

    @Test
    fun generateMonthlyReport_createsPdfFile() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Вставляем тестовую категорию
        val testCategory = Category(
            name = "Тестовая категория",
            type = TransactionType.EXPENSE,
            iconRes = null,
            colorHex = "#FF0000"
        )
        db.categoryDao().insert(testCategory)

        // Получаем ID категории
        val categories = db.categoryDao().getAllCategoriesOnce()
        val catId = categories.first { it.name == "Тестовая категория" }.id

        // Вставляем тестовую транзакцию
        db.transactionDao().insert(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 10_000L,
                categoryId = catId,
                date = Instant.now(),
                note = "Тестовая транзакция"
            )
        )

        // Создаём URI (простой способ, работает в тестах)
        val uri = Uri.fromFile(testFile)

        val result = reportGenerator.generateMonthlyReport(
            context,
            uri,
            YearMonth.now()
        )

        assertTrue("PDF generation failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        assertTrue("PDF file was not created", testFile.exists())
        assertTrue("PDF file is empty", testFile.length() > 0)
    }
}