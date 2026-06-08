package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Budget
import com.example.financeapp.data.BudgetDao
import com.example.financeapp.data.Category
import com.example.financeapp.data.CategoryDao
import com.example.financeapp.data.PeriodType
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.analytics.ComparisonViewModel
import com.example.financeapp.ui.budgets.BudgetsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var viewModel: BudgetsViewModel

    private val sampleCategory = Category(
        id = 1L, name = "Еда",
        type = TransactionType.EXPENSE,
        iconRes = null, colorHex = "#FF0000"
    )

    @Before
    fun setup() {
        budgetDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        every { budgetDao.getAllBudgets() } returns flowOf(emptyList())
        every { categoryDao.getAllCategories() } returns flowOf(listOf(sampleCategory))
        every { transactionDao.getSumByTypeAndPeriod(any(), any(), any()) } returns flowOf(0L)
        viewModel = BudgetsViewModel(budgetDao, transactionDao, categoryDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty budgets list is handled`() = runTest {
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.budgets.size)
    }

    @Test
    fun `categories are loaded from dao`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Еда", viewModel.uiState.value.categories[0].name)
        job.cancel()
    }

    @Test
    fun `addBudget calls dao insert`() = runTest {
        coEvery { budgetDao.insert(any()) } answers {
            // просто заглушка
        }
        viewModel.addBudget(categoryId = 42L, limitAmount = 1000L, periodType = PeriodType.MONTHLY)
        advanceUntilIdle()
        coVerify {
            budgetDao.insert(match { it.categoryId == 42L && it.limitAmount == 1000L })
        }
    }

    @Test
    fun `deleteBudget calls dao delete`() = runTest {
        val budget = Budget(id = 1L, categoryId = 1L,
            limitAmount = 10_000_00L, periodType = PeriodType.MONTHLY)
        coEvery { budgetDao.delete(budget) } just Runs
        viewModel.deleteBudget(budget)
        advanceUntilIdle()
        coVerify(exactly = 1) { budgetDao.delete(budget) }
    }

    @Test
    fun `addBudget with WEEKLY period type creates correct budget`() = runTest {
        coEvery { budgetDao.insert(any()) } just Runs
        viewModel.addBudget(
            categoryId = 1L,
            limitAmount = 3_000_00L,
            periodType = PeriodType.WEEKLY
        )
        advanceUntilIdle()
        coVerify { budgetDao.insert(match { it.periodType == PeriodType.WEEKLY }) }
    }

    @Test
    fun `budget with correct categoryId is inserted`() = runTest {
        coEvery { budgetDao.insert(any()) } just Runs
        viewModel.addBudget(
            categoryId = 42L,
            limitAmount = 5_000_00L,
            periodType = PeriodType.MONTHLY
        )
        advanceUntilIdle()
        coVerify { budgetDao.insert(match { it.categoryId == 42L }) }
    }

    @Test
    fun `budget with correct limitAmount is inserted`() = runTest {
        coEvery { budgetDao.insert(any()) } just Runs
        viewModel.addBudget(
            categoryId = 1L,
            limitAmount = 25_000_00L,
            periodType = PeriodType.MONTHLY
        )
        advanceUntilIdle()
        coVerify { budgetDao.insert(match { it.limitAmount == 25_000_00L }) }
    }
}