package com.example.financeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: Long): Budget?

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsOnce(): List<Budget>

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}