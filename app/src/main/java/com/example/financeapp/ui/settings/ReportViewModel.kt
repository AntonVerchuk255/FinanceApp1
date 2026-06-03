package com.example.financeapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.ReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ReportUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedMonth: YearMonth = YearMonth.now()
)

class ReportViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    private val reportGenerator = ReportGenerator(db)

    fun setMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth) }
    }

    fun generateReport(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = reportGenerator.generateMonthlyReport(
                context, uri, _uiState.value.selectedMonth
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = if (result.isSuccess) "PDF-отчёт сохранён"
                    else "Ошибка: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

class ReportViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReportViewModel(db) as T
    }
}