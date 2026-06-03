package com.example.financeapp.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Goal
import com.example.financeapp.data.GoalDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

data class GoalsUiState(
    val goals: List<Goal> = emptyList()
)

class GoalsViewModel(private val goalDao: GoalDao) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = goalDao.getAllGoals()
        .map { GoalsUiState(goals = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalsUiState()
        )

    fun addGoal(name: String, targetAmount: Long, deadline: Instant?, colorHex: String) {
        viewModelScope.launch {
            goalDao.insert(Goal(name = name, targetAmount = targetAmount, deadline = deadline, colorHex = colorHex))
        }
    }

    fun addToGoal(goalId: Long, amount: Long) {
        viewModelScope.launch { goalDao.addToGoal(goalId, amount) }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch { goalDao.delete(goal) }
    }
}

class GoalsViewModelFactory(
    private val goalDao: GoalDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalsViewModel(goalDao) as T
    }
}