package com.example.financeapp.instrumented_tests.dao

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Budget
import com.example.financeapp.data.BudgetDao
import com.example.financeapp.data.PeriodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BudgetDao

    @Before
    fun createDb() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.budgetDao()
    }

    @After
    fun closeDb() { db.close() }

    @Test
    fun insertAndGetBudget() = runTest {
        dao.insert(Budget(categoryId = 1L, limitAmount = 10_000_00L,
            periodType = PeriodType.MONTHLY))
        val budgets = dao.getAllBudgets().first()
        assertEquals(1, budgets.size)
        assertEquals(10_000_00L, budgets[0].limitAmount)
    }

    @Test
    fun deleteBudget() = runTest {
        dao.insert(Budget(categoryId = 1L, limitAmount = 5_000_00L,
            periodType = PeriodType.WEEKLY))
        val budget = dao.getAllBudgets().first()[0]
        dao.delete(budget)
        assertEquals(0, dao.getAllBudgets().first().size)
    }

    @Test
    fun getBudgetByCategory_returnsCorrectBudget() = runTest {
        dao.insert(Budget(categoryId = 1L, limitAmount = 10_000_00L,
            periodType = PeriodType.MONTHLY))
        dao.insert(Budget(categoryId = 2L, limitAmount = 3_000_00L,
            periodType = PeriodType.WEEKLY))
        val budget = dao.getBudgetByCategory(2L)
        assertEquals(3_000_00L, budget?.limitAmount)
        assertEquals(PeriodType.WEEKLY, budget?.periodType)
    }

    @Test
    fun getBudgetByCategory_notFound_returnsNull() = runTest {
        val budget = dao.getBudgetByCategory(999L)
        assertNull(budget)
    }

    @Test
    fun updateBudget_changesLimitAmount() = runTest {
        dao.insert(Budget(categoryId = 1L, limitAmount = 10_000_00L,
            periodType = PeriodType.MONTHLY))
        val budget = dao.getAllBudgets().first()[0]
        dao.update(budget.copy(limitAmount = 15_000_00L))
        val updated = dao.getAllBudgets().first()[0]
        assertEquals(15_000_00L, updated.limitAmount)
    }

    @Test
    fun multipleBudgetsForDifferentCategories() = runTest {
        dao.insert(Budget(categoryId = 1L, limitAmount = 10_000_00L, periodType = PeriodType.MONTHLY))
        dao.insert(Budget(categoryId = 2L, limitAmount = 5_000_00L, periodType = PeriodType.MONTHLY))
        dao.insert(Budget(categoryId = 3L, limitAmount = 2_000_00L, periodType = PeriodType.WEEKLY))
        val budgets = dao.getAllBudgets().first()
        assertEquals(3, budgets.size)
    }
}