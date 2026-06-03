package com.example.financeapp.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Category
import com.example.financeapp.data.CategoryDao
import com.example.financeapp.data.Reminder
import com.example.financeapp.data.ReminderDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val categories: List<Category> = emptyList()
)

class RemindersViewModel(
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> = combine(
        reminderDao.getAllReminders(),
        categoryDao.getAllCategories()
    ) { reminders, categories ->
        RemindersUiState(reminders = reminders, categories = categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiState()
    )

    fun addReminder(
        title: String,
        amount: Long,
        categoryId: Long?,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            reminderDao.insert(
                Reminder(
                    title = title,
                    amount = amount,
                    categoryId = categoryId,
                    dayOfMonth = dayOfMonth
                )
            )
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.update(reminder.copy(isActive = !reminder.isActive))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminderDao.delete(reminder) }
    }
}

class RemindersViewModelFactory(
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RemindersViewModel(reminderDao, categoryDao) as T
    }
}