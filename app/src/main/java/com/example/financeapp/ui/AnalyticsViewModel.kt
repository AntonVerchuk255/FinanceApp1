package com.example.financeapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.CategorySum
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class TimelinePoint(val date: Instant, val amount: Long)

data class ChartUiState(
    val expenseCategories: List<CategorySum> = emptyList(),
    val incomeCategories: List<CategorySum> = emptyList(),
    val timeline: List<TimelinePoint> = emptyList(),
    val totalExpense: Long = 0L,
    val totalIncome: Long = 0L,
    val selectedType: TransactionType = TransactionType.EXPENSE
)

class AnalyticsViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _periodStart = MutableStateFlow(Instant.now().minus(30, ChronoUnit.DAYS))
    private val _periodEnd = MutableStateFlow(Instant.now())
    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)

    val chartState: StateFlow<ChartUiState> = combine(
        _periodStart,
        _periodEnd,
        _selectedType
    ) { start, end, type ->
        Triple(start, end, type)
    }.flatMapLatest { (start, end, type) ->
        combine(
            dao.getCategoryDistributionByPeriod(TransactionType.EXPENSE, start, end),
            dao.getCategoryDistributionByPeriod(TransactionType.INCOME, start, end),
            dao.getSumByTypeAndPeriod(TransactionType.EXPENSE, start, end),
            dao.getSumByTypeAndPeriod(TransactionType.INCOME, start, end)
        ) { expenseCats, incomeCats, totalExp, totalInc ->
            ChartUiState(
                expenseCategories = expenseCats,
                incomeCategories = incomeCats,
                totalExpense = totalExp ?: 0L,
                totalIncome = totalInc ?: 0L,
                selectedType = type
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChartUiState()
    )

    fun updatePeriod(start: Instant, end: Instant) {
        _periodStart.value = start
        _periodEnd.value = end
    }

    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
    }
}

class AnalyticsViewModelFactory(
    private val dao: TransactionDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AnalyticsViewModel(dao) as T
    }
}