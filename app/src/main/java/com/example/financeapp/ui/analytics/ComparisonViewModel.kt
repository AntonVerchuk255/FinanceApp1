package com.example.financeapp.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.CategorySum
import com.example.financeapp.data.MonthlyTotal
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class PeriodStats(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val expenseCategories: List<CategorySum> = emptyList(),
    val incomeCategories: List<CategorySum> = emptyList()
)

data class ComparisonUiState(
    val currentPeriod: PeriodStats = PeriodStats(),
    val previousPeriod: PeriodStats = PeriodStats(),
    val monthlyTotalsExpense: List<MonthlyTotal> = emptyList(),
    val monthlyTotalsIncome: List<MonthlyTotal> = emptyList(),
    val currentLabel: String = "Этот месяц",
    val previousLabel: String = "Прошлый месяц",
    val selectedType: TransactionType = TransactionType.EXPENSE
)

class ComparisonViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val _comparisonState = MutableStateFlow(ComparisonUiState())
    val uiState: StateFlow<ComparisonUiState> = _comparisonState

    init {
        loadComparison()
        viewModelScope.launch {
            combine(
                dao.getMonthlyTotals(TransactionType.EXPENSE),
                dao.getMonthlyTotals(TransactionType.INCOME),
                _selectedType
            ) { expense, income, type ->
                Triple(expense, income, type)
            }.collect { (expense, income, type) ->
                _comparisonState.update {
                    it.copy(
                        monthlyTotalsExpense = expense,
                        monthlyTotalsIncome = income,
                        selectedType = type
                    )
                }
            }
        }
    }

    private fun loadComparison() {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val now = YearMonth.now()
            val prev = now.minusMonths(1)

            val currentStart = now.atDay(1).atStartOfDay(zone).toInstant()
            val currentEnd = now.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant()
            val prevStart = prev.atDay(1).atStartOfDay(zone).toInstant()
            val prevEnd = prev.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant()

            val currentExpenseCats = dao.getCategoryTotalsOnce(
                TransactionType.EXPENSE, currentStart, currentEnd)
            val currentIncomeCats = dao.getCategoryTotalsOnce(
                TransactionType.INCOME, currentStart, currentEnd)
            val prevExpenseCats = dao.getCategoryTotalsOnce(
                TransactionType.EXPENSE, prevStart, prevEnd)
            val prevIncomeCats = dao.getCategoryTotalsOnce(
                TransactionType.INCOME, prevStart, prevEnd)

            _comparisonState.update {
                it.copy(
                    currentPeriod = PeriodStats(
                        totalIncome = currentIncomeCats.sumOf { c -> c.total },
                        totalExpense = currentExpenseCats.sumOf { c -> c.total },
                        expenseCategories = currentExpenseCats,
                        incomeCategories = currentIncomeCats
                    ),
                    previousPeriod = PeriodStats(
                        totalIncome = prevIncomeCats.sumOf { c -> c.total },
                        totalExpense = prevExpenseCats.sumOf { c -> c.total },
                        expenseCategories = prevExpenseCats,
                        incomeCategories = prevIncomeCats
                    ),
                    currentLabel = "${now.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ru"))} ${now.year}",
                    previousLabel = "${prev.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ru"))} ${prev.year}"
                )
            }
        }
    }

    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
        _comparisonState.update { it.copy(selectedType = type) }
    }
}

class ComparisonViewModelFactory(
    private val dao: TransactionDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ComparisonViewModel(dao) as T
    }
}