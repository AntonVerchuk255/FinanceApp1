package com.example.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val categoryId: Long?,
    val dayOfMonth: Int,        // день месяца (1-31)
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now()
)