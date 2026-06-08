package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.dashboard.DashboardViewModel
import io.mockk.every
import io.mockk.mockk
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var dao: TransactionDao
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupViewModel(
        totalIncome: Long? = 0L,
        totalExpense: Long? = 0L,
        monthIncome: Long? = 0L,
        monthExpense: Long? = 0L,
        recent: List<Transaction> = emptyList()
    ) {
        every { dao.getTotalIncome() } returns flowOf(totalIncome)
        every { dao.getTotalExpense() } returns flowOf(totalExpense)
        every { dao.getSumByTypeAndPeriod(TransactionType.INCOME, any(), any()) } returns flowOf(monthIncome)
        every { dao.getSumByTypeAndPeriod(TransactionType.EXPENSE, any(), any()) } returns flowOf(monthExpense)
        every { dao.getRecentTransactions(any()) } returns flowOf(recent)
        viewModel = DashboardViewModel(dao)
    }

    @Test
    fun `balance equals income minus expense`() = runTest {


        setupViewModel(totalIncome = 15_000_00L, totalExpense = 4_200_00L)
        val job = launch {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(10_800_00L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `balance is zero when database is empty`() = runTest {
        setupViewModel(totalIncome = null, totalExpense = null)
        advanceUntilIdle()
        assertEquals(0L, viewModel.uiState.value.balance)
    }

    @Test
    fun `balance is negative when expenses exceed income`() = runTest {
        setupViewModel(totalIncome = 5_000_00L, totalExpense = 8_000_00L)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(-3_000_00L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `monthIncome is set from getSumByTypeAndPeriod INCOME`() = runTest {
        setupViewModel(monthIncome = 50_000_00L)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(50_000_00L, viewModel.uiState.value.monthIncome)
        job.cancel()
    }

    @Test
    fun `monthExpense is set from getSumByTypeAndPeriod EXPENSE`() = runTest {
        setupViewModel(monthExpense = 12_500_00L)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(12_500_00L, viewModel.uiState.value.monthExpense)
        job.cancel()
    }

    @Test
    fun `recentTransactions contains last 5 items from dao`() = runTest {
        val transactions = List(5) { i ->
            Transaction(
                id = i.toLong(),
                type = TransactionType.EXPENSE,
                amount = 100L,
                categoryId = 1L,
                date = Instant.now(),
                note = null
            )
        }
        setupViewModel(recent = transactions)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.recentTransactions.size)
        job.cancel()
    }

    @Test
    fun `null income and null expense produce zero balance`() = runTest {
        setupViewModel(totalIncome = null, totalExpense = null)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(0L, state.balance)
        assertEquals(0L, state.monthIncome)
        assertEquals(0L, state.monthExpense)
    }

    @Test
    fun `initial uiState has zero balance before data loads`() = runTest {
        setupViewModel()
        // Не вызываем advanceUntilIdle — проверяем начальное состояние
        assertEquals(0L, viewModel.uiState.value.balance)
    }

    @Test
    fun `income only with no expense gives positive balance`() = runTest {
        setupViewModel(totalIncome = 100_000_00L, totalExpense = 0L)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(100_000_00L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `empty recent transactions list is handled correctly`() = runTest {
        setupViewModel(recent = emptyList())
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.recentTransactions.size)
    }
}