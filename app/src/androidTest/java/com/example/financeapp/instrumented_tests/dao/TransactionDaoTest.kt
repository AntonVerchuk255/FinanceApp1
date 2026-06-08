package com.example.financeapp.instrumented_tests.dao

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun createDb() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.transactionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun makeTransaction(
        type: TransactionType = TransactionType.EXPENSE,
        amount: Long = 1_000_00L,
        categoryId: Long? = null,
        date: Instant = Instant.now(),
        note: String? = null
    ) = Transaction(
        type = type,
        amount = amount,
        categoryId = categoryId,
        date = date,
        note = note,
        createdAt = Instant.now()
    )

    @Test
    fun insertAndGetAllTransactions() = runTest {
        dao.insert(makeTransaction(amount = 500_00L))
        val result = dao.getAllTransactions().first()
        assertEquals(1, result.size)
        assertEquals(500_00L, result[0].amount)
    }

    @Test
    fun insertMultipleAndGetAll() = runTest {
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 10_000_00L))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE, amount = 3_000_00L))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE, amount = 1_500_00L))
        val result = dao.getAllTransactions().first()
        assertEquals(3, result.size)
    }

    @Test
    fun deleteTransaction() = runTest {
        dao.insert(makeTransaction())
        val inserted = dao.getAllTransactions().first()[0]
        dao.delete(inserted)
        val result = dao.getAllTransactions().first()
        assertEquals(0, result.size)
    }

    @Test
    fun getTotalIncome_returnsCorrectSum() = runTest {
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 10_000_00L))
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 5_000_00L))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE, amount = 2_000_00L))
        val total = dao.getTotalIncome().first()
        assertEquals(15_000_00L, total)
    }

    @Test
    fun getTotalExpense_returnsCorrectSum() = runTest {
        dao.insert(makeTransaction(type = TransactionType.EXPENSE, amount = 3_000_00L))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE, amount = 2_500_00L))
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 50_000_00L))
        val total = dao.getTotalExpense().first()
        assertEquals(5_500_00L, total)
    }

    @Test
    fun getTotalIncome_emptyDb_returnsNull() = runTest {
        val total = dao.getTotalIncome().first()
        assertNull(total)
    }

    @Test
    fun getSumByTypeAndPeriod_returnsCorrectAggregation() = runTest {
        val now = Instant.now()
        val monthAgo = now.minus(30, ChronoUnit.DAYS)
        val twoMonthsAgo = now.minus(61, ChronoUnit.DAYS)

        // В периоде
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 10_000_00L, date = now))
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 5_000_00L,
            date = now.minus(15, ChronoUnit.DAYS)))
        // Вне периода — не должна войти в сумму
        dao.insert(makeTransaction(type = TransactionType.INCOME, amount = 99_999_00L, date = twoMonthsAgo))

        val sum = dao.getSumByTypeAndPeriod(TransactionType.INCOME, monthAgo, now).first()
        assertEquals(15_000_00L, sum)
    }

    @Test
    fun getRecentTransactions_limitsCount() = runTest {
        repeat(10) { dao.insert(makeTransaction()) }
        val result = dao.getRecentTransactions(5).first()
        assertEquals(5, result.size)
    }

    @Test
    fun sortOrder_latestFirst() = runTest {
        val old = Instant.now().minus(2, ChronoUnit.DAYS)
        val new = Instant.now()
        dao.insert(makeTransaction(amount = 100_00L, date = old))
        dao.insert(makeTransaction(amount = 200_00L, date = new))
        val result = dao.getAllTransactions().first()
        assertEquals(200_00L, result[0].amount) // новая транзакция первой
    }

    @Test
    fun updateTransaction_changesAmount() = runTest {
        dao.insert(makeTransaction(amount = 1_000_00L))
        val inserted = dao.getAllTransactions().first()[0]
        dao.update(inserted.copy(amount = 2_000_00L))
        val updated = dao.getAllTransactions().first()[0]
        assertEquals(2_000_00L, updated.amount)
    }

    @Test
    fun categoryDistribution_groupsByCategory() = runTest {
        val now = Instant.now()
        val monthAgo = now.minus(30, ChronoUnit.DAYS)

        // Добавляем категорию
        val categoryDao = db.categoryDao()
        categoryDao.insert(Category(id = 1, name = "Еда",
            type = TransactionType.EXPENSE, iconRes = null, colorHex = "#FF0000"))
        categoryDao.insert(Category(id = 2, name = "Транспорт",
            type = TransactionType.EXPENSE, iconRes = null, colorHex = "#0000FF"))

        dao.insert(makeTransaction(type = TransactionType.EXPENSE,
            amount = 5_000_00L, categoryId = 1L, date = now))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE,
            amount = 3_000_00L, categoryId = 1L, date = now))
        dao.insert(makeTransaction(type = TransactionType.EXPENSE,
            amount = 1_500_00L, categoryId = 2L, date = now))

        val distribution = dao.getCategoryDistributionByPeriod(
            TransactionType.EXPENSE, monthAgo, now).first()

        assertEquals(2, distribution.size)
        // Первая категория должна быть Еда (8000 > 1500)
        assertEquals("Еда", distribution[0].name)
        assertEquals(8_000_00L, distribution[0].total)
    }
}