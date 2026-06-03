package com.example.financeapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val backupManager = BackupManager(db)

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isLoading = true)
            val result = backupManager.exportBackup(context, uri)
            _uiState.value = SettingsUiState(
                message = if (result.isSuccess) "Резервная копия сохранена"
                else "Ошибка: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isLoading = true)
            val result = backupManager.importBackup(context, uri)
            _uiState.value = SettingsUiState(
                message = if (result.isSuccess) "Данные восстановлены"
                else "Ошибка: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

class SettingsViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(db) as T
    }
}