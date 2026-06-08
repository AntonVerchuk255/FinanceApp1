package com.example.financeapp.instrumented_tests.dao

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.*
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
class ReportGeneratorSimpleTest {

    private lateinit var db: AppDatabase
    private lateinit var reportGenerator: ReportGenerator
    private lateinit var testFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Используем inMemoryDatabase
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        reportGenerator = ReportGenerator(db)
        testFile = File(context.cacheDir, "test_report.pdf")

        // Очищаем перед тестом
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @After
    fun cleanup() {
        testFile.delete()
        db.close()
    }

    @Test
    fun generateReport_withTestData_success() = runBlocking {
        // Вставляем тестовую категорию
        val category = Category(
            name = "Тест",
            type = TransactionType.EXPENSE,
            iconRes = null,
            colorHex = "#FF0000"
        )
        db.categoryDao().insert(category)

        val categories = db.categoryDao().getAllCategoriesOnce()
        val catId = categories.first().id

        // Вставляем тестовую транзакцию
        db.transactionDao().insert(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 10_000L,
                categoryId = catId,
                date = Instant.now(),
                note = "Тест"
            )
        )

        val uri = Uri.fromFile(testFile)
        val result = reportGenerator.generateMonthlyReport(
            ApplicationProvider.getApplicationContext(),
            uri,
            YearMonth.now()
        )

        assertTrue("PDF should be generated successfully", result.isSuccess)
        assertTrue("PDF file should exist", testFile.exists())
        assertTrue("PDF file should not be empty", testFile.length() > 0)
    }

    @Test
    fun generateReport_withEmptyData_success() = runBlocking {
        val uri = Uri.fromFile(testFile)
        val result = reportGenerator.generateMonthlyReport(
            ApplicationProvider.getApplicationContext(),
            uri,
            YearMonth.now()
        )

        assertTrue("PDF should be generated even with empty data", result.isSuccess)
        assertTrue("PDF file should be created", testFile.exists())
    }
}