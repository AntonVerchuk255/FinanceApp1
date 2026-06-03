package com.example.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Long,      // лимит в копейках
    val periodType: PeriodType  // MONTHLY, WEEKLY
)

enum class PeriodType { WEEKLY, MONTHLY }