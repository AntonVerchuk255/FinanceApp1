package com.example.financeapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.temporal.ChronoUnit

data class DashboardUiState(
    val balance: Long = 0L,
    val monthIncome: Long = 0L,
    val monthExpense: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList()
)

class DashboardViewModel(private val dao: TransactionDao) : ViewModel() {

    private val monthStart = Instant.now().minus(30, ChronoUnit.DAYS)
    private val now = Instant.now()

    val uiState: StateFlow<DashboardUiState> = combine(
        dao.getTotalIncome(),
        dao.getTotalExpense(),
        dao.getSumByTypeAndPeriod(TransactionType.INCOME, monthStart, now),
        dao.getSumByTypeAndPeriod(TransactionType.EXPENSE, monthStart, now),
        dao.getRecentTransactions(5)
    ) { totalIncome, totalExpense, monthIncome, monthExpense, recent ->
        DashboardUiState(
            balance = (totalIncome ?: 0L) - (totalExpense ?: 0L),
            monthIncome = monthIncome ?: 0L,
            monthExpense = monthExpense ?: 0L,
            recentTransactions = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

class DashboardViewModelFactory(
    private val dao: TransactionDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(dao) as T
    }
}