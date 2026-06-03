package com.example.financeapp.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class BudgetWithSpent(
    val budget: Budget,
    val categoryName: String,
    val spent: Long
)

data class BudgetsUiState(
    val budgets: List<BudgetWithSpent> = emptyList(),
    val categories: List<Category> = emptyList()
)

class BudgetsViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetDao.getAllBudgets(),
        categoryDao.getAllCategories()
    ) { budgets, categories ->
        Pair(budgets, categories)
    }.flatMapLatest { (budgets, categories) ->
        val now = Instant.now()
        val monthStart = now.minus(30, ChronoUnit.DAYS)

        if (budgets.isEmpty()) {
            flowOf(BudgetsUiState(categories = categories))
        } else {
            combine(
                budgets.map { budget ->
                    transactionDao.getSumByTypeAndPeriod(
                        TransactionType.EXPENSE,
                        monthStart,
                        now
                    ).map { spent ->
                        BudgetWithSpent(
                            budget = budget,
                            categoryName = categories.find { it.id == budget.categoryId }?.name ?: "—",
                            spent = spent ?: 0L
                        )
                    }
                }
            ) { it.toList() }
                .map { BudgetsUiState(budgets = it, categories = categories) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState()
    )

    fun addBudget(categoryId: Long, limitAmount: Long, periodType: PeriodType) {
        viewModelScope.launch {
            budgetDao.insert(Budget(categoryId = categoryId, limitAmount = limitAmount, periodType = periodType))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { budgetDao.delete(budget) }
    }
}

class BudgetsViewModelFactory(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BudgetsViewModel(budgetDao, transactionDao, categoryDao) as T
    }
}