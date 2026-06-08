package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.CategorySum
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.AnalyticsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var dao: TransactionDao
    private lateinit var viewModel: AnalyticsViewModel


    private val sampleExpenseCategories = listOf(
        CategorySum(name = "Еда", total = 12_000_00L),
        CategorySum(name = "Транспорт", total = 4_500_00L),
        CategorySum(name = "Развлечения", total = 3_000_00L)
    )

    private val sampleIncomeCategories = listOf(
        CategorySum(name = "Зарплата", total = 80_000_00L)
    )

    @Before
    fun setup() {
        //Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        every { dao.getCategoryDistributionByPeriod(TransactionType.EXPENSE, any(), any()) } returns
                flowOf(sampleExpenseCategories)
        every { dao.getCategoryDistributionByPeriod(TransactionType.INCOME, any(), any()) } returns
                flowOf(sampleIncomeCategories)
        every { dao.getSumByTypeAndPeriod(TransactionType.EXPENSE, any(), any()) } returns
                flowOf(19_500_00L)
        every { dao.getSumByTypeAndPeriod(TransactionType.INCOME, any(), any()) } returns
                flowOf(80_000_00L)
        viewModel = AnalyticsViewModel(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expense categories are loaded correctly`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }
        advanceUntilIdle()
        assertEquals(3, viewModel.chartState.value.expenseCategories.size)
        job.cancel()
    }

    @Test
    fun `income categories are loaded correctly`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()
        assertEquals(1, viewModel.chartState.value.incomeCategories.size)
        job.cancel()
    }

    @Test
    fun `totalExpense is set from dao`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()
        assertEquals(19_500_00L, viewModel.chartState.value.totalExpense)
        job.cancel()
    }

    @Test
    fun `totalIncome is set from dao`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()
        assertEquals(80_000_00L, viewModel.chartState.value.totalIncome)
        job.cancel()
    }

    @Test
    fun `default selectedType is EXPENSE`() = runTest {
        advanceUntilIdle()
        assertEquals(TransactionType.EXPENSE, viewModel.chartState.value.selectedType)
    }

    @Test
    fun `setSelectedType changes selectedType to INCOME`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()
        viewModel.setSelectedType(TransactionType.INCOME)
        advanceUntilIdle()
        assertEquals(TransactionType.INCOME, viewModel.chartState.value.selectedType)
        job.cancel()
    }

    @Test
    fun `expense categories are sorted by total descending`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()

        val categories = viewModel.chartState.value.expenseCategories
        assertTrue(categories[0].total >= categories[1].total)
        assertTrue(categories[1].total >= categories[2].total)
        job.cancel()
    }

    @Test
    fun `updatePeriod triggers data reload`() = runTest {
        val job = launch {
            viewModel.chartState.collect {}
        }

        advanceUntilIdle()
        val newStart = Instant.now().minus(7, ChronoUnit.DAYS)
        val newEnd = Instant.now()
        viewModel.updatePeriod(newStart, newEnd)
        advanceUntilIdle()
        // После смены периода данные перезапрашиваются — проверяем что categories не пустые
        assertEquals(3, viewModel.chartState.value.expenseCategories.size)
        job.cancel()
    }

    @Test
    fun `empty expense categories returns empty list`() = runTest {
        every { dao.getCategoryDistributionByPeriod(TransactionType.EXPENSE, any(), any()) } returns
                flowOf(emptyList())
        viewModel = AnalyticsViewModel(dao)
        advanceUntilIdle()
        assertEquals(0, viewModel.chartState.value.expenseCategories.size)
    }
}