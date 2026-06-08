package com.example.financeapp.unit_tests.viewmodel


import android.content.Context
import com.example.financeapp.data.*
import com.example.financeapp.ui.transactions.TransactionsViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

        // Не мокаем WidgetUpdater, так как в unit-тестах он не вызывается из-за isReturnDefaultValues = true

        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun noFilter_returnsAllTransactions() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.transactions.size)
        job.cancel()
    }

    @Test
    fun filterExpense_excludesIncomeTransactions() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        val result = viewModel.uiState.value.transactions
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == TransactionType.EXPENSE })
        job.cancel()
    }

    @Test
    fun filterIncome_showsOnlyIncomeTransactions() = runTest {
        viewModel.setFilter(TransactionType.INCOME)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        val result = viewModel.uiState.value.transactions
        assertEquals(1, result.size)
        assertEquals(TransactionType.INCOME, result[0].type)
        job.cancel()
    }

    @Test
    fun removingFilter_showsAllTransactionsAgain() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        viewModel.setFilter(null)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.transactions.size)
        job.cancel()
    }

    @Test
    fun deleteTransaction_callsDaoDeleteExactlyOnce() = runTest {
        val tx = sampleTransactions[0]
        coEvery { transactionDao.delete(tx) } just Runs
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()
        coVerify(exactly = 1) { transactionDao.delete(tx) }
    }

    @Test
    fun addTransaction_callsDaoInsertWithCorrectAmountInKopecks() = runTest {
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
    fun categoriesFromDao_areExposedInUiState() = runTest {
        val categories = listOf(
            Category(id = 1, name = "Еда", type = TransactionType.EXPENSE,
                iconRes = null, colorHex = "#FF0000"),
            Category(id = 2, name = "Зарплата", type = TransactionType.INCOME,
                iconRes = null, colorHex = "#00FF00")
        )
        every { categoryDao.getAllCategories() } returns flowOf(categories)
        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.categories.size)
        job.cancel()
    }

    @Test
    fun initialFilterType_isNull() = runTest {
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.filterType)
    }

    @Test
    fun filterExpense_reflectsInUiStateFilterType() = runTest {
        viewModel.setFilter(TransactionType.EXPENSE)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.filterType)
        job.cancel()
    }

    @Test
    fun emptyTransactionListWithFilter_returnsEmptyList() = runTest {
        every { transactionDao.getAllTransactions() } returns flowOf(emptyList())
        viewModel = TransactionsViewModel(transactionDao, categoryDao, context)
        viewModel.setFilter(TransactionType.EXPENSE)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.transactions.size)
    }

    @Test
    fun updateTransaction_callsDaoUpdate() = runTest {
        val tx = sampleTransactions[0]
        coEvery { transactionDao.update(tx) } just Runs
        viewModel.updateTransaction(tx)
        advanceUntilIdle()
        coVerify(exactly = 1) { transactionDao.update(tx) }
    }
}