package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.dashboard.DashboardViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

    private val sampleTransactions = listOf(
        Transaction(
            id = 1, type = TransactionType.INCOME, amount = 50_000_00L,
            categoryId = 1L, date = Instant.now(), note = "Зарплата"
        ),
        Transaction(
            id = 2, type = TransactionType.EXPENSE, amount = 1_500_00L,
            categoryId = 2L, date = Instant.now(), note = "Продукты"
        )
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        every { dao.getTotalIncome() } returns flowOf(50_000_00L)
        every { dao.getTotalExpense() } returns flowOf(1_500_00L)
        every { dao.getSumByTypeAndPeriod(TransactionType.INCOME, any(), any()) } returns flowOf(50_000_00L)
        every { dao.getSumByTypeAndPeriod(TransactionType.EXPENSE, any(), any()) } returns flowOf(1_500_00L)
        every { dao.getRecentTransactions(any()) } returns flowOf(sampleTransactions)
        viewModel = DashboardViewModel(dao)
    }

    @Test
    fun `balance is income minus expense`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(50_000_00L - 1_500_00L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `monthIncome reflects getSumByTypeAndPeriod INCOME`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(50_000_00L, viewModel.uiState.value.monthIncome)
        job.cancel()
    }

    @Test
    fun `monthExpense reflects getSumByTypeAndPeriod EXPENSE`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(1_500_00L, viewModel.uiState.value.monthExpense)
        job.cancel()
    }

    @Test
    fun `recentTransactions are loaded from dao`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.recentTransactions.size)
        job.cancel()
    }

    @Test
    fun `balance is zero when no transactions`() = runTest {
        every { dao.getTotalIncome() } returns flowOf(null)
        every { dao.getTotalExpense() } returns flowOf(null)
        every { dao.getSumByTypeAndPeriod(any(), any(), any()) } returns flowOf(null)
        every { dao.getRecentTransactions(any()) } returns flowOf(emptyList())
        viewModel = DashboardViewModel(dao)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(0L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `negative balance when expenses exceed income`() = runTest {
        every { dao.getTotalIncome() } returns flowOf(1_000_00L)
        every { dao.getTotalExpense() } returns flowOf(5_000_00L)
        viewModel = DashboardViewModel(dao)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(-4_000_00L, viewModel.uiState.value.balance)
        job.cancel()
    }

    @Test
    fun `initial uiState has empty recentTransactions`() {
        // До подписки initialValue должен быть пустым
        assertEquals(emptyList<Transaction>(), viewModel.uiState.value.recentTransactions)
    }

    @Test
    fun `initial uiState has zero balance`() {
        assertEquals(0L, viewModel.uiState.value.balance)
    }

    @Test
    fun `recentTransactions contain correct transaction types`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        val types = viewModel.uiState.value.recentTransactions.map { it.type }
        assert(types.contains(TransactionType.INCOME))
        assert(types.contains(TransactionType.EXPENSE))
        job.cancel()
    }
}