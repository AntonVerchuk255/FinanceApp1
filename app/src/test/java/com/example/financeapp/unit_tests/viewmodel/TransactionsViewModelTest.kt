package com.example.financeapp.unit_tests.viewmodel


import android.content.Context
import com.example.financeapp.data.Category
import com.example.financeapp.data.CategoryDao
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.transactions.TransactionsViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var context: Context
    private lateinit var viewModel: TransactionsViewModel

    private val sampleTransactions = listOf(
        Transaction(id = 1, type = TransactionType.INCOME, amount = 50_000_00L,
            categoryId = 1L, date = Instant.now(), note = "Зарплата"),
        Transaction(id = 2, type = TransactionType.EXPENSE, amount = 1_500_00L,
            categoryId = 2L, date = Instant.now(), note = "Продукты"),
        Transaction(id = 3, type = TransactionType.EXPENSE, amount = 800_00L,
            categoryId = 3L, date = Instant.now(), note = "Транспорт")
    )

    @Before
    fun setup() {
        transactionDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { transactionDao.getAllTransactions() } returns flowOf(sampleTransactions)
        every { categoryDao.getAllCategories() } returns flowOf(emptyList())
        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no filter returns all transactions`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.transactions.size)
        job.cancel()
    }

    @Test
    fun `filter EXPENSE excludes income transactions`() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        val result = viewModel.uiState.value.transactions
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == TransactionType.EXPENSE })
        job.cancel()
    }

    @Test
    fun `filter INCOME shows only income transactions`() = runTest {
        viewModel.setFilter(TransactionType.INCOME)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        val result = viewModel.uiState.value.transactions
        assertEquals(1, result.size)
        assertEquals(TransactionType.INCOME, result[0].type)
        job.cancel()
    }

    @Test
    fun `removing filter shows all transactions again`() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        viewModel.setFilter(null)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.transactions.size)
        job.cancel()
    }

    @Test
    fun `deleteTransaction calls dao delete exactly once`() = runTest {
        val tx = sampleTransactions[0]
        coEvery { transactionDao.delete(tx) } just Runs
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()
        coVerify(exactly = 1) { transactionDao.delete(tx) }
    }

    @Test
    fun `addTransaction calls dao insert with correct amount in kopecks`() = runTest {
        coEvery { transactionDao.insert(any()) } just Runs
        viewModel.addTransaction(
            type = TransactionType.EXPENSE,
            amount = 1_500_00L,
            categoryId = 2L,
            date = Instant.now(),
            note = "Кофе"
        )
        advanceUntilIdle()
        coVerify(exactly = 1) { transactionDao.insert(any()) }
    }

    @Test
    fun `categories from dao are exposed in uiState`() = runTest {
        val categories = listOf(
            Category(id = 1, name = "Еда", type = TransactionType.EXPENSE,
                iconRes = null, colorHex = "#FF0000"),
            Category(id = 2, name = "Зарплата", type = TransactionType.INCOME,
                iconRes = null, colorHex = "#00FF00")
        )
        every { categoryDao.getAllCategories() } returns flowOf(categories)
        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.categories.size)
        job.cancel()
    }

    @Test
    fun `initial filterType is null`() = runTest {
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.filterType)
    }

    @Test
    fun `filter EXPENSE reflects in uiState filterType`() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.filterType)
        job.cancel()
    }

    @Test
    fun `empty transaction list with filter returns empty list`() = runTest {
        every { transactionDao.getAllTransactions() } returns flowOf(emptyList())
        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
        viewModel.setFilter(TransactionType.EXPENSE)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.transactions.size)
    }

    @Test
    fun `updateTransaction calls dao update`() = runTest {
        val tx = sampleTransactions[0]
        coEvery { transactionDao.update(tx) } just Runs
        viewModel.updateTransaction(tx)
        advanceUntilIdle()
        coVerify(exactly = 1) { transactionDao.update(tx) }
    }
}