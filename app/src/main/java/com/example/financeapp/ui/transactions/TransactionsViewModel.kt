package com.example.financeapp.ui.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Category
import com.example.financeapp.data.CategoryDao
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.widget.WidgetUpdater
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filterType: TransactionType? = null
)

class TransactionsViewModel(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) : ViewModel() {

    private val _filterType = MutableStateFlow<TransactionType?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionDao.getAllTransactions(),
        categoryDao.getAllCategories(),
        _filterType
    ) { transactions, categories, filter ->
        TransactionsUiState(
            transactions = if (filter == null) transactions
            else transactions.filter { it.type == filter },
            categories = categories,
            filterType = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    fun setFilter(type: TransactionType?) {
        _filterType.value = type
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Long,
        categoryId: Long?,
        date: Instant,
        note: String?
    ) {
        viewModelScope.launch {
            transactionDao.insert(
                Transaction(
                    type = type,
                    amount = amount,
                    categoryId = categoryId,
                    date = date,
                    note = note
                )
            )
            WidgetUpdater.update(context) // ← обновляем виджет
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
        }
    }
}

class TransactionsViewModelFactory(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(transactionDao, categoryDao, context) as T
    }
}