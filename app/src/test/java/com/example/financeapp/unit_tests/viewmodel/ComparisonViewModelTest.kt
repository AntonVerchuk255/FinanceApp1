package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.MonthlyTotal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.analytics.ComparisonViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComparisonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var dao: TransactionDao
    private lateinit var viewModel: ComparisonViewModel

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        // Мокаем все вызовы, которые используются в init и loadComparison
        coEvery { dao.getCategoryTotalsOnce(any(), any(), any()) } returns emptyList()
        coEvery { dao.getMonthlyTotals(any()) } returns flowOf(emptyList())

        viewModel = ComparisonViewModel(dao)
    }

    @Test
    fun `setSelectedType changes state`() = runTest {
        viewModel.setSelectedType(TransactionType.INCOME)
        advanceUntilIdle()
        assertEquals(TransactionType.INCOME, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `monthly totals are loaded`() = runTest {
        val mockTotals = listOf(MonthlyTotal("2025-01", 10000L))
        coEvery { dao.getMonthlyTotals(TransactionType.EXPENSE) } returns flowOf(mockTotals)

        viewModel = ComparisonViewModel(dao) // пересоздаем
        advanceUntilIdle()

        assertEquals(mockTotals, viewModel.uiState.value.monthlyTotalsExpense)
    }
}