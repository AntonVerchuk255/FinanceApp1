package com.example.financeapp.instrumented_tests.dao

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private lateinit var db: AppDatabase
    private lateinit var backupManager: BackupManager
    private lateinit var testFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Используем inMemoryDatabase
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        backupManager = BackupManager(db)
        testFile = File(context.cacheDir, "test_backup.json")

        runBlocking {
            clearDatabase()
        }
    }

    @After
    fun cleanup() {
        testFile.delete()
        db.close()
    }

    private suspend fun clearDatabase() {
        db.transactionDao().deleteAll()
        db.categoryDao().deleteAll()
        db.budgetDao().deleteAll()
        db.goalDao().deleteAll()
    }

    @Test
    fun exportBackup_createsValidJsonFile() = runBlocking {
        // Добавляем тестовые данные
        val category = Category(name = "Тест", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#FF0000")
        db.categoryDao().insert(category)

        val uri = Uri.fromFile(testFile)
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = backupManager.exportBackup(context, uri)

        assertTrue("Export failed", result.isSuccess)
        assertTrue("Backup file not created", testFile.exists())
        assertTrue("Backup file is empty", testFile.length() > 0)
    }

    @Test
    fun exportAndImportBackup_restoresData() = runBlocking {
        // Добавляем тестовые данные
        val category = Category(name = "Тест", type = TransactionType.EXPENSE, iconRes = null, colorHex = "#FF0000")
        db.categoryDao().insert(category)

        val categoriesBefore = db.categoryDao().getAllCategoriesOnce()
        assertTrue(categoriesBefore.isNotEmpty())

        // Экспортируем
        val uri = Uri.fromFile(testFile)
        val context = ApplicationProvider.getApplicationContext<Context>()
        backupManager.exportBackup(context, uri)

        // Очищаем
        clearDatabase()
        val categoriesAfterClear = db.categoryDao().getAllCategoriesOnce()
        assertEquals(0, categoriesAfterClear.size)

        // Импортируем
        val importResult = backupManager.importBackup(context, uri)
        assertTrue("Import failed", importResult.isSuccess)

        // Проверяем
        val categoriesAfterImport = db.categoryDao().getAllCategoriesOnce()
        assertTrue(categoriesAfterImport.isNotEmpty())
    }
}