package com.example.financeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY dayOfMonth ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND dayOfMonth = :day")
    suspend fun getActiveRemindersByDay(day: Int): List<Reminder>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersOnce(): List<Reminder>
}