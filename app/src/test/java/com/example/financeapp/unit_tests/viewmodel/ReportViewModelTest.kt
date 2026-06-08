package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.MonthlyTotal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.analytics.ComparisonViewModel
import com.example.financeapp.ui.settings.ReportViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class ReportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var db: AppDatabase
    private lateinit var viewModel: ReportViewModel

    @Before
    fun setup() {
        db = mockk(relaxed = true)
        viewModel = ReportViewModel(db)
    }

    @Test
    fun `setMonth updates state`() = runTest {
        val newMonth = YearMonth.of(2025, 6)
        viewModel.setMonth(newMonth)
        assertEquals(newMonth, viewModel.uiState.value.selectedMonth)
    }
}